use std::path::{Path,PathBuf};
use std::io::Write;
use std::fs;
use std::os::unix::fs::{OpenOptionsExt,symlink};
use std::collections::HashMap;

use AppImg;
use Result;
use util::*;
use systemd;

lazy_static!{
    static ref APPIMG_BASE_PATH: PathBuf = PathBuf::from("/storage/appimg");
    static ref APPIMG_RUN_PATH: PathBuf = PathBuf::from("/run/appimg");
}


pub struct ImageManager {
    images: HashMap<String, AppImg>,
    default: Option<String>,
    current: Option<String>,
}


impl ImageManager {
    fn new() -> Result<ImageManager> {
        let default = default_appimg()?;
        let current = current_appimg()?;
        Ok(ImageManager {
            images: HashMap::new(),
            default, current,
        })
    }

    pub fn load() -> Result<ImageManager> {
        let mut manager = ImageManager::new()?;
        for dent in fs::read_dir(APPIMG_BASE_PATH.as_path())? {
            let path = dent?.path();
            manager.process_path(&path)?;
        }
        Ok(manager)
    }

    fn process_path(&mut self, path: &Path) -> Result<()> {
        let meta = path.symlink_metadata()?;
        if !meta.is_dir() {
            return Ok(())
        }
        let name = path_filename(path);
        if !is_valid_name(name) {
            warn!("ignoring directory in appimg storage which has invalid appimg name: {}", name);
            return Ok(())
        }

        let appimg = AppImg::new(name)?;
        println!("adding: {}", appimg.name());
        self.images.insert(appimg.name().to_string(), appimg);
        Ok(())
    }

    pub fn list(&self) -> Result<()> {
        for img in self.images.values() {
            let cur = if self.is_current(img) { "(current)" } else { "" };
            let def = if self.is_default(img) { "(default)" } else { "" };
            let run = if img.is_running() { "[running]"} else {""};
            println!("  {}  {}  {}  {}", img.name(), run, def, cur);
        }

        Ok(())
    }

    pub fn start_default(&mut self) -> Result<()> {
        let name = match self.default {
            Some(ref s) => s.clone(),
            None => bail!("No default image to start"),
        };
        self.start_image(&name)

    }

    pub fn start_image(&mut self, name: &str) -> Result<()> {
        match self.images.get(name) {
            Some(img) => {
                img.start()?;
            },
            None => {
                warn!("Cannot start '{}'. Image does not exist");
                return Ok(())
            },
        }
        // if current is not set, set it to this one
        if self.current.is_none() {
            self.set_current(name)?;
            systemd::systemctl_restart("desktopd");
        }
        Ok(())
    }

    pub fn stop_image(&mut self, name: &str) -> Result<()> {
        match self.images.get(name) {
            Some(img) => img.stop(),
            None => {
                warn!("Cannot stop '{}'. Image does not exist");
                return Ok(())
            },
        }
        let current = match self.current {
            Some(ref s) => s.clone(),
            None => return Ok(()),
        };
        if current == name {
            systemd::systemctl_stop("desktopd");
            let path = APPIMG_RUN_PATH.join("current.appimg");
            if path.exists() {
                fs::remove_file(&path)?;
            }
            if let Some(img_name) = self.find_running_image_name() {
                self.set_current(&img_name)?;
                systemd::systemctl_start("desktopd");
            }
        }
        Ok(())
    }

    fn find_running_image_name(&self) -> Option<String> {
        for img in self.images.values() {
            if img.is_running() {
                return Some(img.name().to_string());
            }
        }
        None
    }

    fn is_current(&self, img: &AppImg) -> bool {
        self.same_name(img, &self.current)
    }

    fn is_default(&self, img: &AppImg) -> bool {
        self.same_name(img, &self.default)
    }

    fn same_name(&self, img: &AppImg,  name: &Option<String>) -> bool {
        if let Some(ref name) = *name {
            name == img.name()
        } else {
            false
        }
    }
    pub fn image_exists(&self, name: &str) -> bool {
        self.images.contains_key(name)
    }


    pub fn set_default(&mut self, name: &str) -> Result<()> {
        if !is_valid_name(name) {
            warn!("{} is not a valid image name", name);
            return Ok(())
        }

        if let Some(ref default) = self.default {
            if default == name {
                warn!("{} is already default appimg", name);
                return Ok(())
            }
        }

        let path = APPIMG_BASE_PATH.join("default.appimg");
        if path.exists() {
            fs::remove_file(&path)?;
        }
        symlink(name, &path)?;
        self.default = Some(name.to_string());
        Ok(())
    }

    pub fn set_current(&mut self, name: &str) -> Result<()> {
        if !is_valid_name(name) {
            warn!("{} is not a valid image name", name);
            return Ok(())
        }
        if let Some(ref current) = self.current {
            if current == name {
                warn!("{} is already current appimg", name);
                return Ok(())
            }
        }

        fs::create_dir_all(APPIMG_RUN_PATH.as_path())?;
        let path = APPIMG_RUN_PATH.join("current.appimg");
        let target = APPIMG_BASE_PATH.join(name);
        if path.exists() {
            fs::remove_file(&path)?;
        }
        symlink(&target, &path)?;

        let script = format!("#!/bin/bash\nmachinectl -E DESKTOP_STARTUP_ID=${{DESKTOP_STARTUP_ID}} shell user@{} /usr/libexec/launch $@\n", name);
        let script_path = APPIMG_RUN_PATH.join("run-in-image");
        let mut f = fs::OpenOptions::new()
            .create(true)
            .write(true)
            .mode(0o755)
            .open(&script_path)?;

        f.write_all(script.as_bytes())?;
        self.current = Some(name.to_string());
        Ok(())
    }
}

fn appimg_symlink(symlink: &Path) -> Result<Option<String>> {
    if !symlink.exists() {
        return Ok(None);
    }

    if !symlink.symlink_metadata()?.file_type().is_symlink() {
        bail!("{} exists but it is not a symlink", symlink.display());
    }

    let link = fs::read_link(&symlink)?;

    let appimg_name = appimg_name_for_symlink_target(&link)?;
    if !is_valid_name(&appimg_name) {
        bail!("symlink {} points to a directory with a name ({}) that is not a valid appimg name", symlink.display(), appimg_name);
    }

    Ok(Some(appimg_name))
}

fn default_appimg() -> Result<Option<String>> {
    appimg_symlink(&APPIMG_BASE_PATH.join("default.appimg"))
}

fn current_appimg() -> Result<Option<String>> {
    appimg_symlink(&APPIMG_RUN_PATH.join("current.appimg"))
}

///
/// Returns a name only if target points to some subdirectory of APPIMG_BASE_PATH
///
fn appimg_name_for_symlink_target(target: &Path) -> Result<String> {
    let path = if target.is_absolute() {
        target.to_path_buf()
    } else if target.components().count() == 1 {
        APPIMG_BASE_PATH.join(target)
    } else {
        bail!("symlink target has invalid value: {}", target.display())
    };

    match path.parent() {
        Some(parent) => {
            if parent != APPIMG_BASE_PATH.as_path() {
                bail!("symlink target points outside of /storage/appimg directory");
            }
        },
        None => {
            bail!("symlink target has invalid value (no parent): {}", target.display())
        },
    };

    if !path.is_dir() {
        bail!("symlink target {} is not a directory", path.display());
    }
    Ok(path_filename(&path).to_string())
}
