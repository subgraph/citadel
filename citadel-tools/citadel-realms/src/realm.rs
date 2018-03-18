use std::path::{PathBuf,Path};
use std::rc::Rc;
use std::cmp::Ordering;
use std::cell::{RefCell,Cell};
use std::fs::{self,File};
use std::os::unix::fs::{symlink,MetadataExt};

use {RealmConfig,Result,Systemd,NetworkConfig};
use util::*;
use appimg::*;

const REALMS_BASE_PATH: &str = "/realms";
const REALMS_RUN_PATH: &str = "/run/realms";

#[derive(Clone)]
pub struct Realm {
    /// The realm name.  Corresponds to a directory with path /realms/realm-$name/
    name: String,

    /// modify time of timestamp file which is updated when realm is set to current.
    ts: Cell<i64>,

    /// Configuration options, either default values or values read from file /realms/realm-$name/config
    config: RealmConfig,

    /// wrapper around various calls to systemd utilities
    systemd: Systemd,

    /// reads and manages 'current' and 'default' symlinks, shared between all instances
    symlinks: Rc<RefCell<RealmSymlinks>>,
}

impl Realm {
    pub fn new(name: &str, symlinks: Rc<RefCell<RealmSymlinks>>, network: Rc<RefCell<NetworkConfig>>) -> Result<Realm> {
        let mut realm = Realm {
            name: name.to_string(),
            ts: Cell::new(0),
            systemd: Systemd::new(network),
            config: RealmConfig::default(), symlinks,
        };
        realm.load_config()?;
        realm.load_timestamp()?;
        Ok(realm)
    }

    fn load_config(&mut self) -> Result<()> {
        let path = self.base_path().join("config");
        self.config = RealmConfig::load_or_default(&path)
            .map_err(|e| format_err!("failed to load realm config file {}: {}", path.display(), e))?;
        Ok(())
    }

    pub fn config(&self) -> &RealmConfig {
        &self.config
    }

    pub fn base_path(&self) -> PathBuf {
        PathBuf::from(REALMS_BASE_PATH).join(format!("realm-{}", self.name))
    }

    pub fn set_default(&self) -> Result<()> {
        if self.is_default() {
            info!("Realm '{}' is already default realm", self.name());
            return Ok(())
        }
        self.symlinks.borrow_mut().set_default_symlink(&self.name)?;
        info!("Realm '{}' set as default realm", self.name());
        Ok(())
    }

    pub fn set_current(&self) -> Result<()> {
        if self.is_current() {
            info!("Realm '{}' is already current realm", self.name());
            return Ok(())
        }
        if !self.is_running()? {
            self.start()?;
        }
        self.symlinks.borrow_mut().set_current_symlink(Some(&self.name))?;
        self.systemd.restart_desktopd()?;
        self.update_timestamp()?;
        info!("Realm '{}' set as current realm", self.name());
        Ok(())
    }

    pub fn is_default(&self) -> bool {
        self.symlinks.borrow().is_name_default(&self.name)
    }

    pub fn is_current(&self) -> bool {
        self.symlinks.borrow().is_name_current(&self.name)
    }

    pub fn is_running(&self) -> Result<bool> {
        self.systemd.realm_is_active(self)
    }

    pub fn run(&self, args: &[String], use_launcher: bool) -> Result<()> {
        self.systemd.machinectl_shell(self, args, use_launcher)?;
        Ok(())
    }

    pub fn exec_shell(&self, as_root: bool) -> Result<()> {
        self.systemd.machinectl_exec_shell(self, as_root)
    }

    pub fn start(&self) -> Result<()> {
        self.systemd.start_realm(self)?;
        info!("Started realm '{}'", self.name());
        Ok(())
    }

    pub fn stop(&self) -> Result<()> {
        self.systemd.stop_realm(self)?;
        if self.is_current() {
            self.symlinks.borrow_mut().set_current_symlink(None)?;
        }
        info!("Stopped realm '{}'", self.name());
        Ok(())
    }

    pub fn name(&self) -> &str {
        &self.name
    }

    fn load_timestamp(&self) -> Result<()> {
        let tstamp = self.base_path().join(".tstamp");
        if tstamp.exists() {
            let meta = tstamp.metadata()?;
            self.ts.set(meta.mtime());
        }
        Ok(())
    }

    /// create an empty file which is used to track the time at which
    /// this realm was last made 'current'.  These times are used
    /// to order the output when listing realms.
    fn update_timestamp(&self) -> Result<()> {
        let tstamp = self.base_path().join(".tstamp");
        if tstamp.exists() {
            fs::remove_file(&tstamp)?;
        }
        File::create(&tstamp)
            .map_err(|e| format_err!("failed to create timestamp file {}: {}", tstamp.display(), e))?;
        // also load the new value
        self.load_timestamp()?;
        Ok(())
    }

    pub fn create_realm_directory(&self) -> Result<()> {
        if self.base_path().exists() {
            bail!("realm base directory {} already exists, cannot create", self.base_path().display());
        }

        fs::create_dir(self.base_path())
            .map_err(|e| format_err!("failed to create realm base directory {}: {}", self.base_path().display(), e))?;

        self.create_home_directory()
            .map_err(|e| format_err!("failed to create realm home directory {}: {}", self.base_path().join("home").display(), e))?;

        // This must be last step because if an error is returned caller assumes that subvolume was
        // never created and does not need to be removed.
        clone_base_appimg(self)?;
        Ok(())
    }

    fn create_home_directory(&self) -> Result<()> {
        let home = self.base_path().join("home");
        mkdir_chown(&home, 1000, 1000)?;
        let skel = PathBuf::from(REALMS_BASE_PATH).join("skel");
        if skel.exists() {
            info!("Populating realm home directory with files from {}", skel.display());
            copy_tree(&skel, &home)?;
        }
        Ok(())
    }

    pub fn delete_realm(&self, save_home: bool) -> Result<()> {
        if save_home {
            self.save_home_for_delete()?;
        }
        if self.is_running()? {
            self.stop()?;
        }
        info!("removing rootfs subvolume for '{}'", self.name());
        delete_rootfs_subvolume(self)?;

        info!("removing realm directory {}", self.base_path().display());
        fs::remove_dir_all(self.base_path())?;

        info!("realm '{}' has been removed", self.name());
        Ok(())
    }

    fn save_home_for_delete(&self) -> Result<()> {
        let target = PathBuf::from(&format!("/realms/removed/home-{}", self.name()));
        if !Path::new("/realms/removed").exists() {
            fs::create_dir("/realms/removed")?;
        }

        fs::rename(self.base_path().join("home"), &target)
            .map_err(|e| format_err!("unable to move realm home directory to {}: {}", target.display(), e))?;
        info!("home directory been moved to /realms/removed/home-{}, delete it at your leisure", self.name());
        Ok(())
    }

}

impl Ord for Realm {
    fn cmp(&self, other: &Realm) -> Ordering {
        let self_run = self.is_running().unwrap_or(false);
        let other_run = other.is_running().unwrap_or(false);

        if self_run && !other_run {
            Ordering::Less
        } else if !self_run && other_run {
            Ordering::Greater
        } else {
            let self_ts = self.ts.get();
            let other_ts = other.ts.get();
            other_ts.cmp(&self_ts)
        }
    }
}

impl PartialOrd for Realm {
    fn partial_cmp(&self, other: &Realm) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl PartialEq for Realm {
    fn eq(&self, other: &Realm) -> bool {
        self.cmp(other) == Ordering::Equal
    }
}

impl Eq for Realm {}

pub struct RealmSymlinks {
    current_name: Option<String>,
    default_name: Option<String>,
}

impl RealmSymlinks {
    pub fn new() -> RealmSymlinks {
        RealmSymlinks {
            current_name: None,
            default_name: None,
        }
    }

    pub fn load_symlinks(&mut self) -> Result<()> {
        self.current_name = self.resolve_realm_name(&PathBuf::from(REALMS_RUN_PATH).join("current.realm"))?;
        self.default_name = self.resolve_realm_name(&PathBuf::from(REALMS_BASE_PATH).join("default.realm"))?;
        Ok(())
    }

    fn is_name_default(&self, name: &str) -> bool {
        match self.default() {
            Some(dname) => dname == name,
            None => false,
        }
    }

    fn is_name_current(&self, name: &str) -> bool {
        match self.current() {
            Some(cname) => cname == name,
            None => false,
        }
    }

    pub fn current(&self) -> Option<String> {
        self.current_name.clone()
    }

    pub fn default(&self) -> Option<String> {
        self.default_name.clone()
    }


    pub fn set_current_symlink(&mut self, name: Option<&str>) -> Result<()> {
        let runpath = Path::new(REALMS_RUN_PATH);
        if !runpath.exists() {
            fs::create_dir_all(runpath)
                .map_err(|e| format_err!("failed to create realm runtime directory {}: {}", runpath.display(), e))?;
        }

        let path = runpath.join("current.realm");
        if let Some(n) = name {
            let tmp = Path::new("/run/current.realm.tmp");
            let target = PathBuf::from(REALMS_BASE_PATH).join(format!("realm-{}", n));
            symlink(&target, tmp)
                .map_err(|e| format_err!("failed to create symlink from {} to {}: {}", tmp.display(), target.display(), e))?;

            fs::rename(tmp, &path)
                .map_err(|e| format_err!("failed to rename symlink from {} to {}: {}", tmp.display(), path.display(), e))?;

            self.current_name = Some(n.to_owned());
        } else {
            if path.exists() {
                fs::remove_file(&path)
                    .map_err(|e| format_err!("failed to remove current symlink {}: {}", path.display(), e))?;
            }
            self.current_name = None;
        }
        Ok(())
    }

    pub fn set_default_symlink(&mut self, name: &str) -> Result<()> {
        let path = PathBuf::from(REALMS_BASE_PATH).join("default.realm");
        let tmp = Path::new("/realms/default.realm.tmp");
        let target = format!("realm-{}", name);
        symlink(&target, tmp)
                .map_err(|e| format_err!("failed to create symlink from {} to {}: {}", tmp.display(), target, e))?;
        fs::rename(tmp, &path)
                .map_err(|e| format_err!("failed to rename symlink from {} to {}: {}", tmp.display(), path.display(), e))?;

        self.default_name = Some(name.to_owned());
        Ok(())
    }

    fn resolve_realm_name(&self, path: &Path) -> Result<Option<String>> {
        if !path.exists() {
            return Ok(None);
        }
        let meta = path.symlink_metadata()?;
        if !meta.file_type().is_symlink() {
            bail!("{} exists but it is not a symlink", path.display());
        }
        let target = RealmSymlinks::absolute_target(path)?;
        RealmSymlinks::ensure_subdir_of_base(path, &target)?;
        if !target.is_dir() {
            bail!("target of symlink {} is not a directory", path.display());
        }
        let filename = path_filename(&target);
        if !filename.starts_with("realm-") {
            bail!("target of symlink {} is not a realm directory", path.display());
        }
        Ok(Some(filename[6..].to_string()))
    }

    /// Read target of symlink `path` and resolve it to an absolute
    /// path
    fn absolute_target(path: &Path) -> Result<PathBuf> {
        let target = fs::read_link(path)?;
        if target.is_absolute() {
            Ok(target)
        } else if target.components().count() == 1 {
            match path.parent() {
                Some(parent) => return Ok(parent.join(target)),
                None => bail!("Cannot resolve absolute path of symlink target because symlink path has no parent"),
            }
        } else {
            bail!("symlink target has invalid value: {}", target.display())
        }
    }

    fn ensure_subdir_of_base(path: &Path, target: &Path) -> Result<()> {
        let realms_base = PathBuf::from(REALMS_BASE_PATH);
        match target.parent() {
            Some(parent) => {
                if parent != realms_base.as_path() {
                    bail!("target of symlink {} points outside of {} directory: {}", path.display(), REALMS_BASE_PATH, target.display());
                }
            },
            None => bail!("target of symlink {} has invalid value (no parent): {}", path.display(), target.display()),
        }
        Ok(())
    }

}


