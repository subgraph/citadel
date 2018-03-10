use std::sync::{Arc,Mutex};
use std::path::Path;
use std::fs;

use failure::ResultExt;

use monitor::{DirectoryMonitor,MonitorEventHandler};
use parser::DesktopFileParser;
use config::Config;
use Result;


pub struct DesktopFileSync {
    config: Config,
    monitor: DirectoryMonitor,
}

impl DesktopFileSync {
    pub fn new(config: Config) -> DesktopFileSync {
        let handler = Arc::new(Mutex::new(SyncHandler::new(config.clone())));
        let monitor = DirectoryMonitor::new(handler.clone());
        DesktopFileSync {
            config, monitor
        }
    }

    pub fn clear_target_directory(&self) -> Result<()> {
        let entries = fs::read_dir(self.config.target_directory())?;

        for entry in entries {
            let path = entry?.path();
            if is_desktop_file(&path) {
                fs::remove_file(&path).context(format!("remove_file({:?})", path))?;
            }
        }
        Ok(())
    }

    pub fn sync_source(&mut self, src: &Path) -> Result<()> {
        self.clear_target_directory()?;
        self.sync_source_directory(src)?;
        self.monitor.set_monitor_sources(&[src.to_path_buf()]);
        Ok(())
    }

    fn sync_source_directory(&self, src: &Path) -> Result<()> {
        let entries = fs::read_dir(src)?;
        for entry in entries {
            let path = entry?.path();
            if is_desktop_file(path.as_path()) {
                if let Err(e) = sync_desktop_file(path.as_path(), &self.config) {
                    info!("error syncing desktop file {:?}: {}", path, e);
                }
            }
        }
        Ok(())
    }
}

struct SyncHandler {
    config: Config,
}

impl SyncHandler {
    fn new(config: Config) -> SyncHandler {
        SyncHandler { config }
    }
}

impl MonitorEventHandler for SyncHandler {
    fn file_added(&self, path: &Path) -> Result<()> {
        info!("file_added: {:?}", path);
        if is_desktop_file(path) {
            sync_desktop_file(path, &self.config)?;
        }
        Ok(())
    }

    fn file_removed(&self, path: &Path) -> Result<()> {
        info!("file_removed: {:?}", path);
        let filename = filename_from_path(path)?;
        let target_path = self.config.target_directory().join(filename);
        if target_path.exists() {
            fs::remove_file(target_path.as_path())?;
        }
        Ok(())
    }
}

fn sync_desktop_file(source: &Path, config: &Config) -> Result<()> {
    if !is_desktop_file(source) {
        return Err(format_err!("source path [{:?}] is not desktop file", source));
    }
    let df = DesktopFileParser::parse_from_path(source, config.exec_prefix())?;
    if df.is_showable() {
        df.write_to_dir(config.target_directory())?;
    } else {
        info!("ignoring {} as not showable", df.filename());
    }
    Ok(())
}

fn is_desktop_file(path: &Path) -> bool {
    if let Some(ext) = path.extension() {
        return ext == "desktop"
    }
    false
}

fn filename_from_path(path: &Path) -> Result<&str> {
    let filename = match path.file_name() {
        Some(name) => name,
        None => return Err(format_err!("Path {:?} has no filename component", path)),
    };
    match filename.to_str() {
        Some(s) => Ok(s),
        None => Err(format_err!("Filename has invalid utf8 encoding")),
    }
}

