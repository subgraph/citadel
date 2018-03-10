#[macro_use] extern crate failure;
#[macro_use] extern crate lazy_static;

extern crate clap;

macro_rules! warn {
    ($e:expr) => { println!("[!]: {}", $e); };
    ($fmt:expr, $($arg:tt)+) => { println!("[!]: {}", format!($fmt, $($arg)+)); };
}

mod appimg;
mod util;
mod systemd;
mod manager;

use failure::Error;
use clap::{App,Arg,ArgMatches,SubCommand};
use clap::AppSettings::*;
use std::process::exit;
use std::result;

pub type Result<T> = result::Result<T,Error>;

pub use appimg::AppImg;
use manager::ImageManager;

fn main() {
    let matches = App::new("citadel-appimg")
        .about("Subgraph Citadel application image management")
        .settings(&[ArgRequiredElseHelp, ColoredHelp, DisableHelpSubcommand, DisableVersion, DeriveDisplayOrder])

        .subcommand(SubCommand::with_name("list")
                    .about("Display list of application images"))

        .subcommand(SubCommand::with_name("start")
                    .about("Launch an application image")
                    .arg(Arg::with_name("name")))

        .subcommand(SubCommand::with_name("stop")
                    .about("Stop a running application image")
                    .arg(Arg::with_name("name").required(true)))

        .subcommand(SubCommand::with_name("default")
                    .about("Set an application image as the default image to boot")
                    .arg(Arg::with_name("name").required(true)))

        .subcommand(SubCommand::with_name("current")
                    .about("Set an application image as 'current'")
                    .arg(Arg::with_name("name").required(true)))

        .get_matches();

    let result = match matches.subcommand() {
        ("list", _) => list_cmd(),
        ("start", Some(m)) => start_cmd(m),
        ("stop", Some(m)) => stop_cmd(m),
        ("default", Some(m)) => default_cmd(m),
        ("current", Some(m)) => current_cmd(m),
        _ => Ok(()),
    };
    if let Err(e) = result {
        println!("{}", e);
        exit(1);
    }
}



fn list_cmd() -> Result<()> {
    let manager = ImageManager::load()?;
    manager.list()?;
    Ok(())
}

fn start_cmd(matches: &ArgMatches) -> Result<()> {
    let mut manager = ImageManager::load()?;
    match matches.value_of("name") {
        Some(name) => manager.start_image(name),
        None => manager.start_default(),
    }
}

fn stop_cmd(matches: &ArgMatches) -> Result<()> {
    let name = matches.value_of("name").unwrap();
    let mut manager = ImageManager::load()?;
    manager.stop_image(name)?;
    Ok(())
}

fn default_cmd(matches: &ArgMatches) -> Result<()> {
    let name = matches.value_of("name").unwrap();
    let mut manager = ImageManager::load()?;
    if manager.image_exists(name) {
        manager.set_default(name)?;
    } else {
        warn!("No image '{}' exists", name);
    }
    Ok(())
}

fn current_cmd(matches: &ArgMatches) -> Result<()> {
    let name = matches.value_of("name").unwrap();
    let mut manager = ImageManager::load()?;
    if manager.image_exists(name) {
        manager.set_current(name)?;
    } else {
        warn!("No image '{}' exists", name);
    }
    Ok(())
}
