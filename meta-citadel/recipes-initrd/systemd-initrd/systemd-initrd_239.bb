

FILESEXTRAPATHS_prepend := "${COREBASE}/meta/recipes-core/systemd/systemd:"
require ${COREBASE}/meta/recipes-core/systemd/systemd_${PV}.bb

PROVIDES_remove = "udev"
RDEPENDS_${PN}_remove = "dbus"

PACKAGECONFIG = "acl cryptsetup usrmerge kmod"

do_install() {
    meson_do_install

    install -d ${D}/sysroot
    [ ! -e ${D}/init ] && ln -s ${rootlibexecdir}/systemd/systemd ${D}/init

    chown root:systemd-journal ${D}/${localstatedir}/log/journal
}

ALTERNATIVE_${PN}_remove = "resolv-conf"

sockets_wants = "${systemd_system_unitdir}/sockets.target.wants"
sysinit_wants = "${systemd_system_unitdir}/sysinit.target.wants"
SYSTEMD_WANTS = "\
    ${sockets_wants}/systemd-journald-audit.socket \
    ${sockets_wants}/systemd-journald.socket \
    ${sockets_wants}/systemd-journald-udevd-control.socket \
    ${sockets_wants}/systemd-journald-udevd-kernel.socket \
    \
    ${sysinit_wants}/kmod-static-nodes.service \
    ${sysinit_wants}/systemd-ask-password-console.path \
    ${sysinit_wants}/systemd-journald.service \
    ${sysinit_wants}/systemd-modules-load.service \
    ${sysinit_wants}/systemd-sysctl.service \
    ${sysinit_wants}/systemd-udev-trigger.service \
    ${sysinit_wants}/systemd-udevd.service \
"
SYSTEMD_TARGETS = "\
    ${systemd_system_unitdir}/sysinit.target \
    ${systemd_system_unitdir}/basic.target \
    ${systemd_system_unitdir}/cryptsetup-pre.target \
    ${systemd_system_unitdir}/cryptsetup.target \
    ${systemd_system_unitdir}/sockets.target \
    ${systemd_system_unitdir}/paths.target \
    ${systemd_system_unitdir}/slices.target \
    ${systemd_system_unitdir}/halt.target \
    ${systemd_system_unitdir}/poweroff.target \
    ${systemd_system_unitdir}/reboot.target \
    ${systemd_system_unitdir}/rescue.target \
    ${systemd_system_unitdir}/shutdown.target \
    ${systemd_system_unitdir}/final.target \
    ${systemd_system_unitdir}/sigpwr.target \
    ${systemd_system_unitdir}/sockets.target \
    ${systemd_system_unitdir}/swap.target \
    ${systemd_system_unitdir}/timers.target \
    ${systemd_system_unitdir}/paths.target \
    ${systemd_system_unitdir}/umount.target \
    ${systemd_system_unitdir}/local-fs.target \
    ${systemd_system_unitdir}/local-fs-pre.target \
"

generators = "${systemd_unitdir}/system-generators"
SYSTEMD_GENERATORS = "\
    ${generators}/systemd-debug-generator \
    ${generators}/systemd-cryptsetup-generator \
    ${generators}/systemd-fstab-generator \
"

SYSTEMD_BINARIES = "\
    ${bindir}/journalctl \
    ${bindir}/systemctl \
    ${bindir}/systemd-ask-password \
    ${bindir}/systemd-run \
    ${bindir}/systemd-escape \
    ${bindir}/systemd-cgls \
    ${bindir}/systemd-tmpfiles \
    ${bindir}/systemd-tty-ask-password-agent \
"

SYSTEMD_UNITS = "\
    ${systemd_system_unitdir}/emergency.target \
    ${systemd_system_unitdir}/emergency.service \
    ${systemd_system_unitdir}/rescue.target \
    ${systemd_system_unitdir}/rescue.service \
    ${systemd_system_unitdir}/debug-shell.service \
    \
    ${systemd_system_unitdir}/initrd.target \
    ${systemd_system_unitdir}/initrd-fs.target \
    ${systemd_system_unitdir}/initrd-root-device.target \
    ${systemd_system_unitdir}/initrd-root-fs.target \
    ${systemd_system_unitdir}/initrd-switch-root.target \
    ${systemd_system_unitdir}/initrd-switch-root.service \
    ${systemd_system_unitdir}/initrd-cleanup.service \
    ${systemd_system_unitdir}/initrd-udevadm-cleanup-db.service \
    ${systemd_system_unitdir}/initrd-parse-etc.service \
    ${systemd_system_unitdir}/kmod-static-nodes.service \
    ${systemd_system_unitdir}/systemd-ask-password-console.path \
    ${systemd_system_unitdir}/systemd-ask-password-console.service \
    ${systemd_system_unitdir}/systemd-journald.service \
    ${systemd_system_unitdir}/systemd-ask-password-plymouth.path \
    ${systemd_system_unitdir}/systemd-ask-password-plymouth.service \
    ${systemd_system_unitdir}/systemd-fsck@.service \
    ${systemd_system_unitdir}/systemd-fsck-root.service \
    ${systemd_system_unitdir}/systemd-journald.socket \
    ${systemd_system_unitdir}/systemd-journald-audit.socket \
    ${systemd_system_unitdir}/systemd-udevd-control.socket \
    ${systemd_system_unitdir}/systemd-udevd-kernel.socket \
    ${systemd_system_unitdir}/systemd-modules-load.service \
    ${systemd_system_unitdir}/systemd-sysctl.service \
    ${systemd_system_unitdir}/systemd-udevd.service \
    ${systemd_system_unitdir}/systemd-udev-trigger.service \
    ${systemd_system_unitdir}/systemd-udev-settle.service \
    ${systemd_system_unitdir}/systemd-volatile-root \
"

FILES_${PN} = "\
    /init /sysroot \
    ${libdir}/lib* \
    ${libdir}/sysctl.d/50-default.conf \
    ${libdir}/tmpfiles.d/systemd.conf \
    ${systemd_unitdir}/libsystemd* \
    ${systemd_unitdir}/systemd* \
    ${base_sbindir} \
    ${SYSTEMD_WANTS} \
    ${SYSTEMD_TARGETS} \
    ${SYSTEMD_UNITS} \
    ${SYSTEMD_GENERATORS} \
    ${SYSTEMD_BINARIES} \
"
python populate_packages_prepend() {
    # hack to sabotage do_split_packages
    d.setVar("rootlibdir", "/nowhere")
}

PRIVATE_LIBS = "libsystemd-shared-${PV}.so libsystemd.so.0 libudev.so.1"

PACKAGES = "${PN} ${PN}-dbg ${PN}-dev ${PN}-unused"
RDEPENDS_${PN}-unused += "bash ${PN}"
FILES_${PN}-unused = "/"


