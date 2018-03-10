
use std::process::Command;

const SYSTEMCTL_PATH: &str = "/usr/bin/systemctl";

pub fn sysctl_is_active(name: &str) -> bool {
    let mut cmd = Command::new(SYSTEMCTL_PATH);
    cmd.arg("-q");
    cmd.arg("is-active");
    cmd.arg(name);
    match cmd.status() {
        Ok(status) => status.success(),
        Err(e) => {
            warn!("failed to execute /usr/bin/systemctl: {}", e);
            false
        }
    }
}

pub fn systemctl_restart(name: &str) -> bool {
    run_systemctl("restart", name)
}

pub fn systemctl_start(name: &str) -> bool {
    run_systemctl("start", name)
}

pub fn systemctl_stop(name: &str) -> bool {
    run_systemctl("stop", name)
}

fn run_systemctl(op: &str, name: &str) -> bool {
    let mut cmd = Command::new(SYSTEMCTL_PATH);
    cmd.arg(op);
    cmd.arg(name);
    match cmd.output() {
        Err(e) => {
            warn!("failed to execute /usr/bin/systemctl: {}", e);
            false
        }
        Ok(output) => {
            if !output.status.success() {
                warn!("error running systemctl {}: {}", op, String::from_utf8(output.stderr).unwrap());
                return false
            }
            true
        }
    }
}

pub fn generate_nspawn_file(extra_bind_mounts: &str) -> String {
    NSPAWN_FILE_TEMPLATE.replace("$EXTRA_BIND_MOUNTS", extra_bind_mounts)
}

pub fn generate_service_file(appimg_name: &str) -> String {
    APPIMG_SERVICE_TEMPLATE.replace("$APPIMG_NAME", appimg_name)
}

pub const NSPAWN_FILE_TEMPLATE: &str = r###"
[Exec]
Boot=true
Environment=IFCONFIG_IP=172.17.0.2/24
Environment=IFCONFIG_GW=172.17.0.1

[Files]
BindReadOnly=/usr/share/themes/Adapta
BindReadOnly=/usr/share/themes/Adapta-Eta
BindReadOnly=/usr/share/themes/Adapta-Nokto
BindReadOnly=/usr/share/themes/Adapta-Nokto-Eta
BindReadOnly=/usr/share/icons/Paper

BindReadOnly=/storage/citadel-state/resolv.conf:/etc/resolv.conf

#
# Bind mounts for sound and pulse audio
#
Bind=/dev/snd
Bind=/dev/shm
BindReadOnly=/run/user/1000/pulse:/run/user/host/pulse

BindReadOnly=/tmp/.X11-unix
BindReadOnly=/run/user/1000/wayland-0:/run/user/host/wayland-0

$EXTRA_BIND_MOUNTS

#
# Uncomment to enable kvm access in container
#
#Bind=/dev/kvm

#
# Uncomment to enable GPU access in container
#
#Bind=/dev/dri/renderD128

[Network]
Zone=clear
"###;

pub const APPIMG_SERVICE_TEMPLATE: &str = r###"
[Unit]
Description=Application Image $APPIMG_NAME instance
Wants=desktopd.service

[Service]
Environment=SYSTEMD_NSPAWN_SHARE_NS_IPC=1
ExecStart=/usr/bin/systemd-nspawn --quiet --keep-unit --machine=$APPIMG_NAME --link-journal=try-guest --directory=/storage/appimg/$APPIMG_NAME/rootfs

KillMode=mixed
Type=notify
RestartForceExitStatus=133
SuccessExitStatus=133
"###;
