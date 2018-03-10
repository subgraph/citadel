use std::path::{Path,PathBuf};
use std::fs::File;
use std::io::Read;
use Result;
use toml;

#[derive(Clone)]
pub struct Config {
    exec_prefix: String,
    target_directory: PathBuf,
    source_paths: Vec<PathBuf>,
}


impl Config {
    pub fn from_path<P: AsRef<Path>>(path: P) -> Result<Config> {
        let toml = ConfigToml::from_path(path)?;
        let config = toml.to_config()?;
        Ok(config)
    }

    pub fn exec_prefix(&self) -> &str {
        self.exec_prefix.as_ref()
    }

    pub fn target_directory(&self) -> &Path {
        self.target_directory.as_ref()
    }

    pub fn source_paths(&self) -> &Vec<PathBuf> {
        self.source_paths.as_ref()
    }
}

#[derive(Deserialize)]
struct ConfigToml {
    options: Option<Options>,
    sources: Option<Vec<Source>>,
}

#[derive(Deserialize)]
struct Options {
    exec_prefix: Option<String>,
    target_directory: Option<String>,
}

impl Options {
    fn exec_prefix(&self) -> Result<&str> {
        match self.exec_prefix {
            Some(ref s) => Ok(s.as_str()),
            None => Err(format_err!("missing 'exec_prefix=' field")),
        }
    }

    fn target_directory(&self) -> Result<&str> {
        match self.target_directory {
            Some(ref s) => Ok(s.as_str()),
            None => Err(format_err!("missing 'target_directory=' field")),
        }
    }
}

#[derive(Deserialize,Clone)]
struct Source {
    path: Option<String>,
}

impl Source {
    fn path(&self) -> Result<&str> {
        match self.path {
            Some(ref s) => Ok(s.as_str()),
            None => Err(format_err!("missing 'path=' field")),
        }
    }
}

fn load_as_string(path: &Path) -> Result<String> {
    let mut f = File::open(path)?;
    let mut buffer = String::new();
    f.read_to_string(&mut buffer)?;
    Ok(buffer)
}

impl ConfigToml {
    fn from_path<P: AsRef<Path>>(path: P) -> Result<ConfigToml> {
        let s = load_as_string(path.as_ref())?;
        let config = toml::from_str::<ConfigToml>(&s)?;
        Ok(config)
    }

    fn options(&self) -> Result<&Options> {
        self.options.as_ref()
            .ok_or(format_err!("missing '[options]' section"))
    }

    fn sources(&self) -> Result<Vec<Source>> {
        match self.sources {
            Some(ref srcs) => Ok(srcs.clone()),
            None => Err(format_err!("missing '[[sources]]' section(s)")),
        }
    }

    fn to_config(&self) -> Result<Config> {
        let options = self.options()?;
        let exec_prefix = options.exec_prefix()?.to_string() + " ";
        let target_path = options.target_directory()?.to_string();
        let target_directory = PathBuf::from(target_path);

        let mut source_paths = Vec::new();
        for src in self.sources()? {
            let path = src.path()?;
            source_paths.push(PathBuf::from(path));
        }
        Ok(Config{
            exec_prefix,
            target_directory,
            source_paths,
        })
    }
}
