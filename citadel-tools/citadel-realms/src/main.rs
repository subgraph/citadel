#[macro_use] extern crate failure;
#[macro_use] extern crate serde_derive;

extern crate libc;
extern crate clap;
extern crate toml;
extern crate termcolor;
extern crate walkdir;

use failure::Error;
use clap::{App,Arg,ArgMatches,SubCommand};
use clap::AppSettings::*;
use std::process::exit;
use std::cell::RefCell;
use std::result;

pub type Result<T> = result::Result<T,Error>;

thread_local! {
    pub static VERBOSE: RefCell<bool> = RefCell::new(true); 
}
pub fn verbose() -> bool {
    VERBOSE.with(|f| *f.borrow())
}

macro_rules! warn {
    ($e:expr) => { println!("[!]: {}", $e); };
    ($fmt:expr, $($arg:tt)+) => { println!("[!]: {}", format!($fmt, $($arg)+)); };
}

macro_rules! info {
    ($e:expr) => { if ::verbose() { println!("[+]: {}", $e); } };
    ($fmt:expr, $($arg:tt)+) => { if ::verbose() { println!("[+]: {}", format!($fmt, $($arg)+)); } };
}

mod manager;
mod realm;
mod util;
mod systemd;
mod config;
mod network;
mod appimg;

use realm::{Realm,RealmSymlinks};
use manager::RealmManager;
use config::RealmConfig;
use systemd::Systemd;
use network::NetworkConfig;

fn main() {
    let app = App::new("citadel-realms")
        .about("Subgraph Citadel realm management")
        .after_help("'realms help <command>' to display help for an individual subcommand\n")
        .global_settings(&[ColoredHelp, DisableVersion, DeriveDisplayOrder, AllowMissingPositional, VersionlessSubcommands ])
        .arg(Arg::with_name("help").long("help").hidden(true))
        .arg(Arg::with_name("quiet")
             .long("quiet")
             .help("Don't display extra output"))

        .subcommand(SubCommand::with_name("list")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Display list of all realms"))

        .subcommand(SubCommand::with_name("shell")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Open shell in current or named realm")

                    .arg(Arg::with_name("realm-name")
                         .help("Name of a realm to open shell in.  Use current realm if omitted."))
                        
                    .arg(Arg::with_name("root-shell")
                         .long("root")
                         .help("Open shell as root instead of user account.")))

        .subcommand(SubCommand::with_name("terminal")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Launch terminal in current or named realm")

                    .arg(Arg::with_name("realm-name")
                         .help("Name of realm to open terminal in. Use current realm if omitted.")))

        .subcommand(SubCommand::with_name("start")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Start named realm or default realm")
                    .arg(Arg::with_name("realm-name")
                         .help("Name of realm to start.  Use default realm if omitted.")))

        .subcommand(SubCommand::with_name("stop")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Stop a running realm by name")
                    .arg(Arg::with_name("realm-name")
                         .required(true)
                         .help("Name of realm to stop.")))

        .subcommand(SubCommand::with_name("default")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Choose a realm to start automatically on boot")
                    .arg(Arg::with_name("realm-name")
                         .help("Name of a realm to set as default.  Display current default realm if omitted.")))

        .subcommand(SubCommand::with_name("current")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Choose a realm to set as 'current' realm")
                    .arg(Arg::with_name("realm-name")
                         .help("Name of a realm to set as current, will start if necessary. Display current realm name if omitted.")))

        .subcommand(SubCommand::with_name("run")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Execute a command in named realm or current realm")

                    .arg(Arg::with_name("realm-name")
                         .help("Name of realm to run command in, start if necessary. Use current realm if omitted."))
                    .arg(Arg::with_name("args")
                         .required(true)
                         .last(true)
                         .allow_hyphen_values(true)
                         .multiple(true)))

        .subcommand(SubCommand::with_name("update-appimg")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Launch shell to update application image")

                    .arg(Arg::with_name("appimg-name")
                         .long("appimg")
                         .help("Name of application image in /storage/appimg directory. Default is to use base.appimg")
                         .takes_value(true)))
                    

        .subcommand(SubCommand::with_name("new")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Create a new realm with the name provided")
                    .arg(Arg::with_name("realm-name")
                         .required(true)
                         .help("Name to assign to newly created realm")))

        .subcommand(SubCommand::with_name("remove")
                    .arg(Arg::with_name("help").long("help").hidden(true))
                    .about("Remove realm by name")

                    .arg(Arg::with_name("no-confirm")
                         .long("no-confirm")
                         .help("Do not prompt for confirmation."))
                    .arg(Arg::with_name("remove-home")
                         .long("remove-home")
                         .help("Also remove home directory with --no-confirm rather than moving it to /realms/removed-homes"))

                    .arg(Arg::with_name("realm-name")
                         .help("Name of realm to remove")
                         .required(true)));



    let matches = app.get_matches();

    if matches.is_present("quiet") {
        VERBOSE.with(|f| *f.borrow_mut() = false);
    }

    let result = match matches.subcommand() {
        ("list", _) => do_list(),
        ("start", Some(m)) => do_start(m),
        ("stop", Some(m)) => do_stop(m),
        ("default", Some(m)) => do_default(m),
        ("current", Some(m)) => do_current(m),
        ("run", Some(m)) => do_run(m),
        ("shell", Some(m)) => do_shell(m),
        ("terminal", Some(m)) => do_terminal(m),
        ("new", Some(m)) => do_new(m),
        ("remove", Some(m)) => do_remove(m),
        ("base-update", _) => do_base_update(),
        _ =>  do_list(),
    };

    if let Err(e) = result {
        warn!("{}", e);
        exit(1);
    }
}

fn is_root() -> bool {
    unsafe {
        libc::geteuid() == 0
    }
}

fn require_root() -> Result<()> {
    if !is_root() {
        bail!("You need to do that as root")
    }
    Ok(())
}

fn do_list() -> Result<()> {
    let manager = RealmManager::load()?;
    println!();
    manager.list()?;
    println!("\n  'realms help' for list of commands\n");
    Ok(())
}

fn do_start(matches: &ArgMatches) -> Result<()> {
    require_root()?;
    let mut manager = RealmManager::load()?;
    match matches.value_of("realm-name") {
        Some(name) => manager.start_named_realm(name)?,
        None => manager.start_default()?,
    };
    Ok(())
}

fn do_stop(matches: &ArgMatches) -> Result<()> {
    require_root()?;
    let name = matches.value_of("realm-name").unwrap();
    let mut manager = RealmManager::load()?;
    manager.stop_realm(name)?;
    Ok(())
}

fn do_default(matches: &ArgMatches) -> Result<()> {
    let manager = RealmManager::load()?;

    match matches.value_of("realm-name") {
        Some(name) => {
            require_root()?;
            manager.set_default_by_name(name)?;
        },
        None => {
            if let Some(name) = manager.default_realm_name() {
                println!("Default Realm: {}", name);
            } else {
                println!("No default realm.");
            }
        },
    }
    Ok(())
}

fn do_current(matches: &ArgMatches) -> Result<()> {
    let manager = RealmManager::load()?;

    match matches.value_of("realm-name") {
        Some(name) => {
            require_root()?;
            manager.set_current_by_name(name)?;
        },
        None => {
            if let Some(name) = manager.current_realm_name() {
                println!("Current Realm: {}", name);
            } else {
                println!("No current realm.");
            }
        },
    }
    Ok(())
}


fn do_run(matches: &ArgMatches) -> Result<()> {
    let args: Vec<&str> = matches.values_of("args").unwrap().collect();
    let mut v = Vec::new();
    for arg in args {
        v.push(arg.to_string());
    }
    let manager = RealmManager::load()?;
    manager.run_in_realm(matches.value_of("realm-name"), &v, true)?;
    Ok(())
}

fn do_shell(matches: &ArgMatches) -> Result<()> {
    let manager = RealmManager::load()?;
    let root = matches.is_present("root-shell");
    manager.launch_shell(matches.value_of("realm-name"), root)?;
    Ok(())
}

fn do_terminal(matches: &ArgMatches) -> Result<()> {
    let manager = RealmManager::load()?;
    manager.launch_terminal(matches.value_of("realm-name"))?;
    Ok(())
}

fn do_new(matches: &ArgMatches) -> Result<()> {
    require_root()?;
    let name = matches.value_of("realm-name").unwrap();
    let mut manager = RealmManager::load()?;
    manager.new_realm(name)?;
    Ok(())
}

fn do_remove(matches: &ArgMatches) -> Result<()> {
    require_root()?;
    let confirm = !matches.is_present("no-confirm");
    let save_home = !matches.is_present("remove-home");
    let name = matches.value_of("realm-name").unwrap();
    let mut manager = RealmManager::load()?;
    manager.remove_realm(name, confirm, save_home)?;
    Ok(())
}

fn do_base_update() -> Result<()> {
    require_root()?;
    let manager = RealmManager::load()?;
    manager.base_appimg_update()
}
