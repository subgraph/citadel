use std::path::Path;
use std::process::Command;

use Realm;
use Result;

const BASE_APPIMG_PATH: &str = "/storage/appimg/base.appimg";
const BTRFS_COMMAND: &str = "/usr/bin/btrfs";

pub fn clone_base_appimg(target_realm: &Realm) -> Result<()> {
    if !Path::new(BASE_APPIMG_PATH).exists() {
        bail!("base appimg does not exist at {}", BASE_APPIMG_PATH);
    }
    let target = format!("/realms/realm-{}/rootfs", target_realm.name());
    let target_path = Path::new(&target);

    if target_path.exists() {
        bail!("cannot create clone of base appimg for realm '{}' because rootfs directory already exists at {}", 
              target_realm.name(), target);
    }

    if !target_path.parent().unwrap().exists() {
        bail!("cannot create clone of base appimg for realm '{}' because realm directory /realms/realm-{} does not exist.", 
              target_realm.name(), target_realm.name());
    }

    Command::new(BTRFS_COMMAND)
        .args(&["subvolume", "snapshot", BASE_APPIMG_PATH, &target ])
        .status()
        .map_err(|e| format_err!("failed to execute {}: {}", BTRFS_COMMAND, e))?;
    Ok(())

}

pub fn delete_rootfs_subvolume(realm: &Realm) -> Result<()> {
    let path = realm.base_path().join("rootfs");
    Command::new(BTRFS_COMMAND)
        .args(&["subvolume", "delete", path.to_str().unwrap() ])
        .status()
        .map_err(|e| format_err!("failed to execute {}: {}", BTRFS_COMMAND, e))?;
    Ok(())
}

