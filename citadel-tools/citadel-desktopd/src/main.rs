#[macro_use] extern crate failure;
#[macro_use] extern crate lazy_static;
#[macro_use] extern crate log;
#[macro_use] extern crate serde_derive;
extern crate env_logger;
extern crate serde;
extern crate toml;
extern crate inotify;
extern crate nix;

mod desktop;
mod parser;
mod config;
mod monitor;
mod desktop_file_sync;

use std::result;
use std::process;
use failure::Error;
use desktop_file_sync::DesktopFileSync;

use config::Config;

pub type Result<T> = result::Result<T, Error>;


fn main() {
    std::env::set_var("RUST_LOG", "info");
    env_logger::init();

    let mut args = std::env::args();
    args.next();
    if args.len() != 1 {
        println!("expected config file argument");
        process::exit(1);
    }

    let config_path = args.next().unwrap();
    let config = match Config::from_path(&config_path) {
        Err(e) => {
            warn!("Failed to load configuration file: {}", e);
            process::exit(1);
        },
        Ok(config) => config,
    };

    let src = config.source_paths().first().unwrap().clone();

    let mut dfs = DesktopFileSync::new(config.clone());
    if let Err(e) = dfs.sync_source(src.as_path()) {
        warn!("error calling sync_source: {}", e);
    }
    loop {
        std::thread::sleep(std::time::Duration::new(120, 0));
    }

}
