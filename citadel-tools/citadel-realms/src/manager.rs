use std::rc::Rc;
use std::cell::RefCell;
use std::path::{Path,PathBuf};
use std::fs;
use std::collections::HashMap;
use std::io::Write;


use Realm;
use Result;
use Systemd;
use RealmSymlinks;
use NetworkConfig;
use util::*;

const REALMS_BASE_PATH: &str = "/realms";

pub struct RealmManager {
    /// Map from realm name -> realm
    realm_map: HashMap<String, Realm>,

    /// Sorted for 'list'
    realm_list: Vec<Realm>,

    /// track status of 'current' and 'default' symlinks
    symlinks: Rc<RefCell<RealmSymlinks>>,

    /// finds free ip addresses to use
    network: Rc<RefCell<NetworkConfig>>,

    /// interface to systemd
    systemd: Systemd,
}


impl RealmManager {
    fn new() -> Result<RealmManager> {
        let network = RealmManager::create_network_config()?;

        Ok(RealmManager {
            realm_map: HashMap::new(),
            realm_list: Vec::new(),
            symlinks: Rc::new(RefCell::new(RealmSymlinks::new())),
            network: network.clone(),
            systemd: Systemd::new(network),
        })
    }

    fn create_network_config() -> Result<Rc<RefCell<NetworkConfig>>> {
        let mut network = NetworkConfig::new();
        network.add_bridge("clear", "172.17.0.0/24")?;
        Ok(Rc::new(RefCell::new(network)))
    }

    pub fn load() -> Result<RealmManager> {
        let mut manager = RealmManager::new()?;
        manager.symlinks.borrow_mut().load_symlinks()?;
        if ! PathBuf::from(REALMS_BASE_PATH).exists() {
            bail!("realms base directory {} does not exist", REALMS_BASE_PATH);
        }
        for dent in fs::read_dir(REALMS_BASE_PATH)? {
            let path = dent?.path();
            manager.process_realm_path(&path)
                .map_err(|e| format_err!("error processing entry {} in realm base dir: {}", path.display(), e))?;
        }
        manager.realm_list.sort_unstable();
        Ok(manager)
    }

    ///
    /// Process `path` as an entry from the base realms directory and 
    /// if `path` is a directory, and directory name has prefix "realm-" 
    /// extract chars after prefix as realm name and add a new `Realm` 
    /// instance
    ///
    fn process_realm_path(&mut self, path: &Path) -> Result<()> {
        let meta = path.symlink_metadata()?;
        if !meta.is_dir() {
            return Ok(())
        }

        let fname = path_filename(path);
        if !fname.starts_with("realm-") {
            return Ok(())
        }

        let (_, realm_name) = fname.split_at(6);
        if !is_valid_realm_name(realm_name) {
            warn!("ignoring directory in realm storage which has invalid realm name: {}", realm_name);
            return Ok(())
        }
        let rootfs = path.join("rootfs");
        if !rootfs.exists() {
            warn!("realm directory {} does not have a rootfs, ignoring", path.display());
            return Ok(())
        }

        match Realm::new(realm_name, self.symlinks.clone(), self.network.clone()) {
            Ok(realm) => { self.add_realm_entry(realm);} ,
            Err(e) => warn!("Ignoring '{}': {}", realm_name, e),
        };
        Ok(())

    }

    fn add_realm_entry(&mut self, realm: Realm) -> &Realm {
        self.realm_map.insert(realm.name().to_owned(), realm.clone());
        self.realm_list.push(realm.clone());
        self.realm_map.get(realm.name()).expect("cannot find realm we just added to map")
    }

    fn remove_realm_entry(&mut self, name: &str) -> Result<()> {
        self.realm_map.remove(name);
        let list = self.realm_list.clone();
        let mut have_default = false;
        self.realm_list.clear();
        for realm in list {
            if realm.name() != name {
                if realm.is_default() {
                    have_default = true;
                }
                self.realm_list.push(realm);
            }
        }
        if !have_default && !self.realm_list.is_empty() {
            self.symlinks.borrow_mut().set_default_symlink(self.realm_list[0].name())?;
        }
        Ok(())
    }

    pub fn current_realm_name(&self) -> Option<String> {
        self.symlinks.borrow().current() 
    }

    pub fn default_realm_name(&self) -> Option<String> {
        self.symlinks.borrow().default() 
    }

    /// 
    /// Execute shell in a realm. If `realm_name` is `None` then exec
    /// shell in current realm, otherwise look up realm by name.
    ///
    /// If `root_shell` is true, open a root shell, otherwise open
    /// a user (uid = 1000) shell.
    ///
    pub fn launch_shell(&self, realm_name: Option<&str>, root_shell: bool) -> Result<()> {
        let run_shell = |realm: &Realm| { 
            info!("opening shell in realm '{}'", realm.name());
            realm.exec_shell(root_shell)?; 
            info!("exiting shell in realm '{}'", realm.name());
            Ok(())
        };

        if let Some(name) = realm_name {
            self.with_named_realm(name, true, run_shell)
        } else {
            self.with_current_realm(run_shell)
        }
    }

    pub fn launch_terminal(&self, name: Option<&str>) -> Result<()> {
        let run_terminal = |realm: &Realm| {
            info!("opening terminal in realm '{}'", realm.name());
            let title_arg = format!("Realm: {}", realm.name());
            realm.run(&["/usr/bin/gnome-terminal".to_owned(), "--title".to_owned(), title_arg], true)
        };

        if let Some(name) = name {
            self.with_named_realm(name, true, run_terminal)
        } else {
            self.with_current_realm(run_terminal)
        }

    }

    pub fn run_in_realm(&self, realm_name: Option<&str>, args: &[String], use_launcher: bool) -> Result<()> {

        if let Some(name) = realm_name {
            self.with_named_realm(name, true, |realm| realm.run(args, use_launcher))
        } else {
            self.with_current_realm(|realm| realm.run(args, use_launcher))
        }
    }

    fn with_current_realm<F: Fn(&Realm)->Result<()>>(&self, f: F) -> Result<()> {
        match self.symlinks.borrow().current() {
            Some(ref name) => {
                self.with_named_realm(name, false, f)?;
            },
            None => {
                warn!("No current realm instance to run command in");
            }
        }
        Ok(())
    }

    fn with_named_realm<F: Fn(&Realm)->Result<()>>(&self, name: &str, want_start: bool, f: F) -> Result<()> {
        match self.realm(name) {
            Some(realm) => {
                if want_start && !realm.is_running()? {
                    info!("realm '{}' is not running, starting it.", realm.name());
                    self.start_realm(realm)?;
                }
                f(realm)
            },
            None => bail!("no realm with name '{}' exists", name),
        }
    }

    pub fn list(&self) -> Result<()> {
        let mut out = ColoredOutput::new();
        self.print_realm_header(&mut out);
        for realm in &self.realm_list {
            self.print_realm(realm, &mut out)?;
        }
        Ok(())
    }

    fn print_realm_header(&self, out: &mut ColoredOutput) {
        out.write("   REALMS     ").bold("bold").write(": current, ").bright("colored")
            .write(": running, (default) starts on boot\n").write("   ------\n\n");
    }

    fn print_realm(&self, realm: &Realm, out: &mut ColoredOutput) -> Result<()> {
        let name = format!("{:12}", realm.name());
        if realm.is_current() {
            out.write(" > ").bold(&name);
        } else if realm.is_running()? {
            out.write("   ").bright(&name);
        } else {
            out.write("   ").dim(&name);
        }

        if realm.is_default() {
            out.write("  (default)");
        }
        out.write("\n");
        Ok(())
    }

    pub fn start_default(&mut self) -> Result<()> {
        let default = self.symlinks.borrow().default();
        if let Some(ref realm_name) = default {
            self.start_named_realm(realm_name)?;
            return Ok(());
        }
        bail!("No default realm to start");
    }

    pub fn start_named_realm(&mut self, realm_name: &str) -> Result<()> {
        info!("starting realm '{}'", realm_name);
        self.with_named_realm(realm_name, false, |realm| self.start_realm(realm))
    }

    fn start_realm(&self, realm: &Realm) -> Result<()> {
        let mut symlinks = self.symlinks.borrow_mut();
        let no_current_realm = symlinks.current().is_none();
        // no realm is current, so make this realm the current one
        // service file for realm will also start desktopd, so this symlink 
        // must be created before launching realm.
        if no_current_realm {
            symlinks.set_current_symlink(Some(realm.name()))?;
        }
        if let Err(e) = realm.start() {
            if no_current_realm {
                // oops realm failed to start, need to reset symlink we changed
                symlinks.set_current_symlink(None)?;
            }
            return Err(e);
        }
        Ok(())
    }
    

    pub fn stop_realm(&mut self, name: &str) -> Result<()> {
        match self.realm_map.get(name) {
            Some(realm) => {
                realm.stop()?;
                self.set_current_if_none()?;
            },
            None => {
                warn!("Cannot stop '{}'. Realm does not exist", name);
                return Ok(())
            },
        };
        Ok(())
    }

    fn set_current_if_none(&self) -> Result<()> {
        let mut symlinks = self.symlinks.borrow_mut();
        if symlinks.current().is_some() {
            return Ok(());
        }

        if let Some(ref name) = self.find_running_realm_name()? {
            symlinks.set_current_symlink(Some(name))?;
            self.systemd.restart_desktopd()?;
        } else {
            self.systemd.stop_desktopd()?;
        }
        Ok(())
    }

    fn find_running_realm_name(&self) -> Result<Option<String>> {
        for realm in self.realm_map.values() {
            if realm.is_running()? {
                return Ok(Some(realm.name().to_string()));
            }
        }
        Ok(None)
    }

    pub fn set_current_by_name(&self, realm_name: &str) -> Result<()> {
        self.with_named_realm(realm_name, false, |realm| realm.set_current())
    }

    pub fn set_default_by_name(&self, realm_name: &str) -> Result<()> {
        self.with_named_realm(realm_name, false, |realm| realm.set_default())
    }
    pub fn realm_name_exists(&self, name: &str) -> bool {
        self.realm_map.contains_key(name)
    }

    pub fn realm(&self, name: &str) -> Option<&Realm> {
        self.realm_map.get(name)
    }

    pub fn new_realm(&mut self, name: &str) -> Result<&Realm> {
        if !is_valid_realm_name(name) {
            bail!("'{}' is not a valid realm name. Only letters, numbers and dash '-' symbol allowed in name. First character must be a letter", name);
        } else if self.realm_name_exists(name) {
            bail!("A realm with name '{}' already exists", name);
        }

        let realm = Realm::new(name, self.symlinks.clone(), self.network.clone())?;

        match realm.create_realm_directory() {
            Ok(()) => Ok(self.add_realm_entry(realm)),
            Err(e) => {
                fs::remove_dir_all(realm.base_path())?;
                Err(e)
            },
        }

    }

    pub fn remove_realm(&mut self, realm_name: &str, confirm: bool, save_home: bool) -> Result<()> {
        self.with_named_realm(realm_name, false, |realm| {
            if realm.base_path().join(".realmlock").exists() {
                warn!("Realm '{}' has .realmlock file in base directory to protect it from deletion.", realm.name());
                warn!("Remove this file from {} before running 'realms remove {}' if you really want to delete it", realm.base_path().display(), realm.name());
                return Ok(());
            }
            let mut save_home = save_home;
            if confirm {
                if !RealmManager::confirm_delete(realm.name(), &mut save_home)? {
                    return Ok(());
                }
            }
            realm.delete_realm(save_home)?;
            self.set_current_if_none()
        })?;

        self.remove_realm_entry(realm_name)?;
        Ok(())
    }

    fn confirm_delete(realm_name: &str, save_home: &mut bool) -> Result<bool> {
        let you_sure = RealmManager::prompt_user(&format!("Are you sure you want to remove realm '{}'?", realm_name), false)?;
        if !you_sure {
            info!("Ok, not removing");
            return Ok(false);
        }

        println!("\nThe home directory for this realm can be saved in /realms/removed/home-{}\n", realm_name);
        *save_home = RealmManager::prompt_user("Would you like to save the home directory?", true)?;
        Ok(true)
    }

    fn prompt_user(prompt: &str, default_y: bool) -> Result<bool> {
        let yn = if default_y { "(Y/n)" } else { "(y/N)" };
        use std::io::{stdin,stdout};
        print!("{} {} : ", prompt, yn);
        stdout().flush()?;
        let mut line = String::new();
        stdin().read_line(&mut line)?;

        let yes = match line.trim().chars().next() {
            Some(c) => c == 'Y' || c == 'y',
            None => default_y,
        };
        Ok(yes)
    }

    pub fn base_appimg_update(&self) -> Result<()> {
        info!("Entering root shell on base appimg");
        self.systemd.base_image_update_shell()
    }
}
