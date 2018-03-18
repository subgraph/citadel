use std::rc::Rc;
use std::cell::RefCell;
use std::process::Command;
use std::path::{Path,PathBuf};
use std::fs::{self,File};
use std::fmt::Write;
use std::io::Write as IoWrite;
use std::env;

const SYSTEMCTL_PATH: &str = "/usr/bin/systemctl";
const MACHINECTL_PATH: &str = "/usr/bin/machinectl";
const SYSTEMD_NSPAWN_PATH: &str = "/run/systemd/nspawn";
const SYSTEMD_UNIT_PATH: &str = "/run/systemd/system";

const DESKTOPD_SERVICE: &str = "citadel-desktopd.service";

use Realm;
use NetworkConfig;
use Result;
use util::{path_filename,is_first_char_alphabetic};

#[derive(Clone)]
pub struct Systemd {
    network: Rc<RefCell<NetworkConfig>>,
}

impl Systemd {

    pub fn new(network: Rc<RefCell<NetworkConfig>>) -> Systemd {
        Systemd { network }
    }

    pub fn realm_is_active(&self, realm: &Realm) -> Result<bool> {
        let active = self.is_active(&self.realm_service_name(realm))?;
        let has_config = self.realm_config_exists(realm);
        if active && !has_config {
            bail!("Realm {} is running, but config files are missing", realm.name());
        }
        if !active && has_config {
            bail!("Realm {} is not running, but config files are present", realm.name());
        }
        Ok(active)
    }

    pub fn start_realm(&self, realm: &Realm) -> Result<()> {
        if self.realm_is_active(realm)? {
            warn!("Realm {} is already running", realm.name());
            return Ok(())
        }
        self.write_realm_launch_config(realm)?;
        self.systemctl_start(&self.realm_service_name(realm))?;
        if realm.config().emphemeral_home() {
            self.setup_ephemeral_home(realm)?;
        }
        
        Ok(())
        
    }

    pub fn base_image_update_shell(&self) -> Result<()> {
        let netconf = self.network.borrow_mut();
        let gw = netconf.gateway("clear")?;
        let addr = netconf.reserved("clear")?;
        let gw_env = format!("--setenv=IFCONFIG_GW={}", gw);
        let addr_env = format!("--setenv=IFCONFIG_IP={}", addr);

        Command::new("/usr/bin/systemd-nspawn")
            .args(&[ 
                  &addr_env, &gw_env, 
                 "--quiet", 
                 "--machine=base-appimg-update", 
                 "--directory=/storage/appimg/base.appimg", 
                 "--network-zone=clear",
                 "/bin/bash", "-c", "/usr/libexec/configure-host0.sh && exec /bin/bash"
            ]).status()?;
        Ok(())
    }

    fn setup_ephemeral_home(&self, realm: &Realm) -> Result<()> {

        // 1) if exists: machinectl copy-to /realms/skel /home/user
        if Path::new("/realms/skel").exists() {
            self.machinectl_copy_to(realm, "/realms/skel", "/home/user")?;
        }

        // 2) if exists: machinectl copy-to /realms/realm-$name /home/user
        let realm_skel = realm.base_path().join("skel");
        if realm_skel.exists() {
            self.machinectl_copy_to(realm, realm_skel.to_str().unwrap(), "/home/user")?;
        }

        let home = realm.base_path().join("home");
        if !home.exists() {
            return Ok(());
        }

        // 3) for each : machinectl bind /realms/realm-$name/home/$dir /home/user/$dir
        for dent in fs::read_dir(home)? {
            let path = dent?.path();
            self.bind_mount_home_subdir(realm, &path)?;
        }

        Ok(())
    }

    fn bind_mount_home_subdir(&self, realm: &Realm, path: &Path) -> Result<()> {
        let path = path.canonicalize()?;
        if !path.is_dir() {
            return Ok(());
        }
        let fname = path_filename(&path);
        if !is_first_char_alphabetic(fname) {
            return Ok(());
        }
        let from = format!("/realms/realm-{}/home/{}", realm.name(), fname);
        let to = format!("/home/user/{}", fname);
        self.machinectl_bind(realm, &from, &to)?; 
        Ok(())
    }

    pub fn stop_realm(&self, realm: &Realm) -> Result<()> {
        if !self.realm_is_active(realm)? {
            warn!("Realm {} is not running", realm.name());
            return Ok(());
        }
        self.systemctl_stop(&self.realm_service_name(realm))?;
        self.remove_realm_launch_config(realm)?;
        self.network.borrow_mut().free_allocation_for(realm.config().network_zone(), realm.name())?;
        Ok(())
    }

    pub fn restart_desktopd(&self) -> Result<bool> {
        self.systemctl_restart(DESKTOPD_SERVICE)
    }
    pub fn stop_desktopd(&self) -> Result<bool> {
        self.systemctl_stop(DESKTOPD_SERVICE)
    }

    fn realm_service_name(&self, realm: &Realm) -> String {
        format!("realm-{}.service", realm.name())
    }

    fn is_active(&self, name: &str) -> Result<bool> {
        Command::new(SYSTEMCTL_PATH)
            .args(&["--quiet", "is-active", name])
            .status()
            .map(|status| status.success())
            .map_err(|e| format_err!("failed to execute{}: {}", MACHINECTL_PATH, e))

    }


    fn systemctl_restart(&self, name: &str) -> Result<bool> {
        self.run_systemctl("restart", name)
    }

    fn systemctl_start(&self, name: &str) -> Result<bool> {
        self.run_systemctl("start", name)
    }

    fn systemctl_stop(&self, name: &str) -> Result<bool> {
        self.run_systemctl("stop", name)
    }

    fn run_systemctl(&self, op: &str, name: &str) -> Result<bool> {
        Command::new(SYSTEMCTL_PATH)
            .arg(op)
            .arg(name)
            .status()
            .map(|status| status.success())
            .map_err(|e| format_err!("failed to execute {}: {}", MACHINECTL_PATH, e))
    }

    fn machinectl_copy_to(&self, realm: &Realm, from: &str, to: &str) -> Result<()> {
        Command::new(MACHINECTL_PATH)
            .args(&["copy-to", realm.name(), from, to ])
            .status()
            .map_err(|e| format_err!("failed to machinectl copy-to {} {} {}: {}", realm.name(), from, to, e))?;
        Ok(())
    }

    fn machinectl_bind(&self, realm: &Realm, from: &str, to: &str) -> Result<()> {
        Command::new(MACHINECTL_PATH)
            .args(&["--mkdir", "bind", realm.name(), from, to ])
            .status()
            .map_err(|e| format_err!("failed to machinectl bind {} {} {}: {}", realm.name(), from, to, e))?;
        Ok(())

    }

    pub fn machinectl_exec_shell(&self, realm: &Realm, as_root: bool) -> Result<()> {
        let namevar = format!("--setenv=REALM_NAME={}", realm.name());
        let user = if as_root { "root" } else { "user" };
        let user_at_host = format!("{}@{}", user, realm.name());
        Command::new(MACHINECTL_PATH)
            .args(&[ &namevar, "--quiet", "shell", &user_at_host, "/bin/bash"])
            .status()
            .map_err(|e| format_err!("failed to execute{}: {}", MACHINECTL_PATH, e))?;

        Ok(())
    }

    pub fn machinectl_shell(&self, realm: &Realm, args: &[String], launcher: bool) -> Result<()> {
        let namevar = format!("--setenv=REALM_NAME={}", realm.name());
        let mut cmd = Command::new(MACHINECTL_PATH);
        cmd.arg("--quiet");
        match env::var("DESKTOP_STARTUP_ID") {
            Ok(val) => {
                cmd.arg("-E");
                cmd.arg(&format!("DESKTOP_STARTUP_ID={}", val));
            },
            Err(_) => {},
        };
        cmd.arg(&namevar);
        cmd.arg("shell");
        cmd.arg(format!("user@{}", realm.name()));

        if launcher {
            cmd.arg("/usr/libexec/launch");
        }

        for arg in args {
            cmd.arg(&arg);
        }
        cmd.status().map_err(|e| format_err!("failed to execute{}: {}", MACHINECTL_PATH, e))?;
        Ok(())
    }


    fn realm_service_path(&self, realm: &Realm) -> PathBuf {
        PathBuf::from(SYSTEMD_UNIT_PATH).join(self.realm_service_name(realm))
    }

    fn realm_nspawn_path(&self, realm: &Realm) -> PathBuf {
        PathBuf::from(SYSTEMD_NSPAWN_PATH).join(format!("{}.nspawn", realm.name()))
    }

    fn realm_config_exists(&self, realm: &Realm) -> bool {
        self.realm_service_path(realm).exists() || self.realm_nspawn_path(realm).exists()
    }

    fn remove_realm_launch_config(&self, realm: &Realm) -> Result<()> {
        let nspawn_path = self.realm_nspawn_path(realm);
        if nspawn_path.exists() {
            fs::remove_file(&nspawn_path)?;
        }
        let service_path = self.realm_service_path(realm);
        if service_path.exists() {
            fs::remove_file(&service_path)?;
        }
        Ok(())
    }

    fn write_realm_launch_config(&self, realm: &Realm) -> Result<()> {
        let nspawn_path = self.realm_nspawn_path(realm);
        let nspawn_content = self.generate_nspawn_file(realm)?;
        self.write_launch_config_file(&nspawn_path, &nspawn_content)
            .map_err(|e| format_err!("failed to write nspawn config file {}: {}", nspawn_path.display(), e))?;

        let service_path = self.realm_service_path(realm);
        let service_content = self.generate_service_file(realm);
        self.write_launch_config_file(&service_path, &service_content)
            .map_err(|e| format_err!("failed to write service config file {}: {}", service_path.display(), e))?;

        Ok(())
    }

    /// Write the string `content` to file `path`. If the directory does
    /// not already exist, create it.
    fn write_launch_config_file(&self, path: &Path, content: &str) -> Result<()> {
        match path.parent() {
            Some(parent) => {
                if !parent.exists() {
                    fs::create_dir_all(parent)?;
                }
            },
            None => bail!("config file path {} has no parent?", path.display()),
        };
        let mut f = File::create(path)?;
        f.write_all(content.as_bytes())?;
        Ok(())
    }

    fn generate_nspawn_file(&self, realm: &Realm) -> Result<String> {
        Ok(NSPAWN_FILE_TEMPLATE
           .replace("$EXTRA_BIND_MOUNTS", &self.generate_extra_bind_mounts(realm)?)

           .replace("$NETWORK_CONFIG", &self.generate_network_config(realm)?))
    }

    fn generate_extra_bind_mounts(&self, realm: &Realm) -> Result<String> {
        let config = realm.config();
        let mut s = String::new();

        if config.emphemeral_home() {
            writeln!(s, "TemporaryFileSystem=/home/user:mode=755,uid=1000,gid=1000")?;
        } else {
            writeln!(s, "Bind={}/home:/home/user", realm.base_path().display())?;
        }

        if config.shared_dir() && Path::new("/realms/Shared").exists() {
            writeln!(s, "Bind=/realms/Shared:/home/user/Shared")?;
        }

        if config.kvm() {
            writeln!(s, "Bind=/dev/kvm")?;
        }

        if config.gpu() {
            writeln!(s, "Bind=/dev/dri/renderD128")?;
        }

        if config.sound() {
            writeln!(s, "Bind=/dev/snd")?;
            writeln!(s, "Bind=/dev/shm")?;
            writeln!(s, "BindReadOnly=/run/user/1000/pulse:/run/user/host/pulse")?;
        }

        if config.x11() {
            writeln!(s, "BindReadOnly=/tmp/.X11-unix")?;
        }

        if config.wayland() {
            writeln!(s, "BindReadOnly=/run/user/1000/wayland-0:/run/user/host/wayland-0")?;
        }

        Ok(s)
    }

    fn generate_network_config(&self, realm: &Realm) -> Result<String> {
        let mut s = String::new();
        if realm.config().network() {
            let mut netconf = self.network.borrow_mut();
            let zone = realm.config().network_zone();
            let addr = netconf.allocate_address_for(zone, realm.name())?;
            let gw = netconf.gateway(zone)?;
            writeln!(s, "Environment=IFCONFIG_IP={}", addr)?;
            writeln!(s, "Environment=IFCONFIG_GW={}", gw)?; 
            writeln!(s, "[Network]")?;
            writeln!(s, "Zone=clear")?;
        } else {
            writeln!(s, "[Network]")?;
            writeln!(s, "Private=true")?;
        }
        Ok(s)
    }

    fn generate_service_file(&self, realm: &Realm) -> String {
        let rootfs = format!("/realms/realm-{}/rootfs", realm.name());
        REALM_SERVICE_TEMPLATE.replace("$REALM_NAME", realm.name()).replace("$ROOTFS", &rootfs)
    }
}


pub const NSPAWN_FILE_TEMPLATE: &str = r###"
[Exec]
Boot=true
$NETWORK_CONFIG

[Files]
BindReadOnly=/usr/share/themes
BindReadOnly=/usr/share/icons/Paper

BindReadOnly=/storage/citadel-state/resolv.conf:/etc/resolv.conf

$EXTRA_BIND_MOUNTS

"###;

pub const REALM_SERVICE_TEMPLATE: &str = r###"
[Unit]
Description=Application Image $REALM_NAME instance
Wants=citadel-desktopd.service

[Service]
Environment=SYSTEMD_NSPAWN_SHARE_NS_IPC=1
ExecStart=/usr/bin/systemd-nspawn --quiet --notify-ready=yes --keep-unit --machine=$REALM_NAME --link-journal=try-guest --directory=$ROOTFS

KillMode=mixed
Type=notify
RestartForceExitStatus=133
SuccessExitStatus=133
"###;
