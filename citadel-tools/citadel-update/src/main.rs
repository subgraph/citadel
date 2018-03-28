#[macro_use] extern crate failure;
#[macro_use] extern crate nix;
#[macro_use] extern crate serde_derive;


extern crate libc;
extern crate clap;
extern crate serde;
extern crate toml;
extern crate ed25519_dalek;
extern crate sha2;
extern crate rand;
extern crate rustc_serialize;


thread_local! {
    pub static VERBOSE: RefCell<bool> = RefCell::new(false);
    pub static SYSOP: RefCell<bool> = RefCell::new(false);
}

pub fn verbose() -> bool {
    VERBOSE.with(|f| {
        *f.borrow()
    })
}

fn sysop() -> bool {
    SYSOP.with(|f| {
        *f.borrow()
    })
}

macro_rules! info {
    ($e:expr) => { if ::verbose() { println!("[+] {}", $e);} };
    ($fmt:expr, $($arg:tt)+) => { if ::verbose() { println!("[+] {}", format!($fmt, $($arg)+));} };
}
macro_rules! warn {
    ($e:expr) => { if ::verbose() { println!("WARNING: {}", $e);} };
    ($fmt:expr, $($arg:tt)+) => { if ::verbose() { println!("WARNING: {}", format!($fmt, $($arg)+));} };
}

macro_rules! notify {
    ($e:expr) => { println!("[+] {}", $e); };
    ($fmt:expr, $($arg:tt)+) => { println!("[+] {}", format!($fmt, $($arg)+)); };
}

use std::result;
use std::process::exit;

use failure::Error;
use clap::{App,Arg,ArgMatches, SubCommand};
use clap::AppSettings::*;
use sha2::Sha512;
use rand::OsRng;
use ed25519_dalek::Keypair;
use rustc_serialize::hex::ToHex;
use std::cell::RefCell;
use std::env;


pub use config::Config;
pub use metainfo::Metainfo;
pub use blockdev::BlockDev;
pub use partition::{Partition,MAX_METAINFO_LEN};
use unpacker::UpdateImageUnpacker;
use packer::UpdateImagePacker;
use boot::BootSelection;

mod boot;
mod metainfo;
mod partition;
mod blockdev;
mod config;
mod packer;
mod unpacker;
mod util;

pub type Result<T> = result::Result<T,Error>;

fn main() {
    match env::var("CITADEL_SYSOP") {
        Ok(_) => SYSOP.with(|f| *f.borrow_mut() = true),
        _ => {},
    };

    let mut app = App::new("citadel-update")
        .about("Subgraph Citadel update and rootfs management")
        .settings(&[ArgRequiredElseHelp, ColoredHelp, DisableHelpSubcommand, DisableVersion, DeriveDisplayOrder])
        .arg(Arg::with_name("v")
             .help("Verbose output")
             .short("v")
             .long("verbose"))
        .arg(Arg::with_name("config")
            .help("Optionally specify an alternate config file")
            .takes_value(true)
            .short("c") .long("config"))

        .subcommand(SubCommand::with_name("list")
            .about("Show information about all rootfs partitions"))

        .subcommand(SubCommand::with_name("which-boot")
            .about("Show which rootfs paritition would currently boot according to the boot selection algorithm"))

        .subcommand(SubCommand::with_name("verify-update")
            .about("Verify the signature of an update image"))

        .subcommand(SubCommand::with_name("update")
            .about("Download update if available and install")
            .arg(Arg::with_name("download-only")
                 .help("Only download available update, don't install")
                 .long("download")))

        .subcommand(SubCommand::with_name("install-update")
            .about("Install an update image")
            .arg(Arg::with_name("image")
                 .required(true)));
    if sysop() {

        app = app.subcommand(SubCommand::with_name("build-update")
            .about("Create an update image from a raw citadel-image.ext2 file")
            .arg(Arg::with_name("image")
                 .required(true)))
                    
        .subcommand(SubCommand::with_name("genkeys")
            .about("Generate a new update keypair"));
        
    }

    let matches = app.get_matches();


    let config = load_config(&matches);

    if matches.is_present("v") {
        VERBOSE.with(|f| *f.borrow_mut() = true);
    }

    let result = match matches.subcommand() {
        ("list", Some(_)) => list_cmd(&config),
        ("which-boot", Some(_)) => { which_boot_cmd(&config)},
        ("update", Some(m)) => update_cmd(&config, m),
        ("verify-update", Some(m)) => verify_update_cmd(&config, m),
        ("install-update", Some(m)) => install_update_cmd(&config, m),
        ("build-update", Some(m)) => build_update_cmd(&config, m),
        ("genkeys", Some(_)) => genkeys_cmd(),
        ("mount-rootfs", Some(m)) => mount_rootfs_cmd(&config, m),
        (s, Some(_)) => {info!("subcommand: {}", s); Ok(())},
        _ => Ok(()),
    };

    if let Err(e) = result {
        println!("{}", e);
        exit(1);
    }
}

fn load_config(arg_matches: &ArgMatches) -> Config {
    let config_load = match arg_matches.value_of("config") {
        Some(path) => Config::load(path),
        None => Config::load_default(),
    };
    match config_load {
        Ok(config) => config,
        Err(e) => {
            println!("{}", e);
            exit(1);
        }
    }
}

fn list_cmd(_config: &Config) -> Result<()> {
    println!("{:^30} {:^14} {:^8} {:^8} {:^12}", "DEVICE PATH", "MOUNTED", "CHANNEL", "VERSION", "STATUS");
    for p in Partition::rootfs_partitions()? {
        let info = partition_info(&p);
        println!("{:^30} {:^14} {:^8} {:^8} {:^12}", info.0, info.1, info.2, info.3, info.4);
    }
    Ok(())
}


fn partition_info(part: &Partition) -> (String,String,String,String,String) {
    let mounted = if part.is_mounted() { "[Yes]".to_string() } else { String::new() };
    let status = if part.is_initialized() { part.status_label() } else { "Not Initialized".to_string() };
    let (channel, version) = match part.metainfo() {
        Ok(meta) => (meta.channel().to_string(), meta.version().to_string()),
        _ => (String::new(), String::new()),
    };
    (part.path_str().to_string(), mounted, channel, version, status)
}

fn mount_rootfs_cmd(_config: &Config, _matches: &ArgMatches) -> Result<()> {
    // mounting installer rootfs should happen here too?
    // perhaps based on cmd line flag
    // maybe define guid like the gpt generator looks for
    // for: rootfs inside luks, rootfs outside luks
    let bs = BootSelection::load_partitions()?;
    let _p = match bs.choose_boot_partition() {
        Some(p) => p,
        None => bail!("None of the rootfs partitions have a bootable image"),
    };
    Ok(())
}

fn which_boot_cmd(_config: &Config) -> Result<()> {
    let select = BootSelection::load_partitions()?;
    match select.choose_boot_partition() {
        Some(part) => {
            notify!("Next boot will be from partition: {}", part.path_str());
        },
        None => {
            warn!("None of the rootfs partitions are currently in bootable state.");
            warn!("Unless a valid image is installed, computer will fail to boot");
        }
    }
    Ok(())
}

fn verify_update_cmd(_config: &Config, _matches: &ArgMatches) -> Result<()> {
    println!("do verify_update");
    Ok(())
}

fn update_cmd(_config: &Config, _matches: &ArgMatches) -> Result<()> {
    // note to self, remember to verify that downloaded update image version matches the version
    // which is expected.
    Ok(())

}

fn install_update_cmd(config: &Config, matches: &ArgMatches) -> Result<()> {
    let image_path = match matches.value_of("image") {
        Some(val) => val,
        None => bail!("install-update requires an image path"),
    };
    let unpack = UpdateImageUnpacker::open(image_path, config)?;
    info!("unpacking image channel: {} version: {}", unpack.metainfo().channel(), unpack.metainfo().version());
    unpack.unpack_disk_image()?;
    info!("decompressing image");
    unpack.decompress_disk_image()?;
    info!("verifying shasum");
    unpack.verify_shasum()?;

    let bs = BootSelection::load_partitions()?;
    let p = match bs.choose_install_partition() {
        Some(p) => p,
        None => bail!("None of the rootfs partitions are available to install update to"),
    };
    info!("installing to {}", p.path_str());
    unpack.write_partition(p)?;
    notify!("Update image successfully installed to {}", p.path_str());


    //update::UpdateImage::new(path);
    // 1) read header, extract metainfo
    // 2) verify signature on metainfo
    // 3) determine if this version/channel makes sense to be installed
    // 4) xtrat image data to temporary file with https://crates.io/crates/lzma-rs
    // 5) verify sha256 on image data
    //
    // 6) choose rootfs partition
    // 7) write partition info block, setting status to INVALID
    // 8) write update image to device
    // 9) run verifyupdate using provided --salt
    // 10) re-rewrite parition info block with status NEW
    Ok(())
}

fn build_update_cmd(config: &Config, matches: &ArgMatches) -> Result<()> {
    let image_path = match matches.value_of("image") {
        Some(val) => val,
        None => bail!("build-update requires an image path"),
    };
    let channel = match config.get_default_channel() {
        Some(ch) => ch,
        None => bail!("Could not determine default channel from config file"),
    };
    let mut builder = UpdateImagePacker::new(config, channel, image_path)?;
    builder.build()?;
    Ok(())
}

fn genkeys_cmd() -> Result<()> {
    let mut rng = OsRng::new()?;
    let keypair = Keypair::generate::<Sha512>(&mut rng);

    println!("pubkey = \"{}\"", keypair.public.to_bytes().to_hex());
    println!("privkey = \"{}\"", keypair.to_bytes().to_hex());

    Ok(())
}
