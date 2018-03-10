
use std::path::{Path,PathBuf};
use std::fs::{self,File};
use std::io::{BufRead,BufReader};

use Result;
use systemd;

use util::*;

lazy_static!{
    static ref APPIMG_BASE_PATH: PathBuf = PathBuf::from("/storage/appimg");
    static ref USERDATA_BASE_PATH: PathBuf = PathBuf::from("/storage/user-data");
    static ref SYSTEMD_UNIT_PATH: PathBuf = PathBuf::from("/run/systemd/system");
    static ref SYSTEMD_NSPAWN_PATH: PathBuf = PathBuf::from("/run/systemd/nspawn");
}



#[derive(Debug)]
pub struct AppImg {
    name: String,
    config: AppImgConfig,
}

impl AppImg {

    pub fn new(name: &str) -> Result<AppImg> {
        let mut img = AppImg {
            name: name.to_string(),
            config: AppImgConfig::new(name),
        };
        img.load_config()?;
        img.write_systemd_files()?;
        Ok(img)
    }

    fn load_config(&mut self) -> Result<()> {
        let mut path = APPIMG_BASE_PATH.join(&self.name);
        path.push("config");
        if !path.exists() {
            return Ok(());
        }
        self.config.load(&path)?;
        Ok(())
    }

    fn write_systemd_files(&self) -> Result<()> {
        if !self.nspawn_config_path().exists() {
            self.write_nspawn_config()?;
        }
        if !self.service_unit_path().exists() {
            self.write_service_unit()?;
        }
        Ok(())
    }

    pub fn is_running(&self) -> bool {
        systemd::sysctl_is_active(&self.service_unit_name())
    }

    pub fn start(&self) -> Result<()> {
        if self.is_running() {
            warn!("image {} is already running", self.name);
            return Ok(());
        }
        self.write_nspawn_config()?;
        self.write_service_unit()?;
        systemd::systemctl_start(&self.service_unit_name());
        Ok(())

    }

    pub fn stop(&self) {
        if !self.is_running() {
            warn!("image {} is not running", self.name);
            return;
        }
        systemd::systemctl_stop(&self.service_unit_name());
    }

    pub fn name(&self) -> &str {
        &self.name
    }

    fn nspawn_config_path(&self) -> PathBuf {
        SYSTEMD_NSPAWN_PATH.join(&format!("{}.nspawn", self.name))
    }

    fn service_unit_name(&self) -> String {
        format!("appimg-{}.service", self.name)
    }

    fn service_unit_path(&self) -> PathBuf {
        SYSTEMD_UNIT_PATH.join(&format!("appimg-{}.service", self.name))
    }

    fn write_nspawn_config(&self) -> Result<()> {
        let mut extra = String::new();
        extra += &format!("Bind={}:/home/user\n", self.config.home());
        if self.config.use_kvm() {
            extra += "Bind=/dev/kvm\n";
        }
        if self.config.use_gpu() {
            extra += "Bind=/dev/dri/renderD128\n";
        }

        let content = systemd::generate_nspawn_file(&extra);
        fs::create_dir_all(SYSTEMD_NSPAWN_PATH.as_path())?;
        write_string_to_file(&self.nspawn_config_path(), &content)?;

        Ok(())
    }

    fn write_service_unit(&self) -> Result<()> {
        let content = systemd::generate_service_file(&self.name);
        fs::create_dir_all(&SYSTEMD_UNIT_PATH.as_path())?;

        write_string_to_file(&self.service_unit_path(), &content)?;
        Ok(())
    }
}

#[derive(Debug)]
pub struct AppImgConfig {
    img_name: String,
    home: Option<String>,
    kvm: bool,
    gpu: bool,
}

const DEFAULT_HOME_PATH: &str = "/storage/user-data/primary-home";

impl AppImgConfig {
    fn new(img_name: &str) -> AppImgConfig {
        AppImgConfig{
            img_name: img_name.to_string(),
            home: None,
            kvm: false,
            gpu: false,
        }
    }

    pub fn home(&self) -> String {
        if let Some(ref name) = self.home {
            if is_valid_name(name) {
                let home_path = USERDATA_BASE_PATH.join(name);
                return home_path.to_str().unwrap().to_string();
            }
        }
        DEFAULT_HOME_PATH.to_string()
    }

    pub fn use_kvm(&self) -> bool {
        self.kvm

    }

    pub fn use_gpu(&self) -> bool {
        self.gpu
    }

    fn reset(&mut self) {
        self.home = None;
        self.kvm = false;
        self.gpu = false;
    }

    fn load(&mut self, path: &Path) -> Result<()> {
        self.reset();
        let f = File::open(path)?;
        let reader = BufReader::new(f);
        for line in reader.lines() {
            let line = line?;
            let v = line.split('=').collect::<Vec<_>>();
            if v.len() == 2 {
                self.process_keyval(v[0].trim(), v[1].trim());

            }
        }
        Ok(())
    }

    fn process_keyval(&mut self, k: &str, v: &str) {
        match k {

            "home" => {
                if is_valid_name(v) {
                    let home_path = USERDATA_BASE_PATH.join(v);
                    if !home_path.is_dir() {
                        warn!("'home' value '{}' in config file of image {} refers to directory that doesn't exist", v, self.img_name);

                    }
                    self.home = Some(v.to_string());
                } else {
                    warn!("Invalid home value '{}' in config file of image {}", v, self.img_name);
                }
            },
            "use-kvm" => {
                self.kvm = v == "yes";

            },

            "use-gpu" => {
                self.gpu = v == "yes";
            },

            _ => {
                warn!("unknown keyword '{}' in config file for image {}", k, self.img_name);
            },
        }
    }


}

