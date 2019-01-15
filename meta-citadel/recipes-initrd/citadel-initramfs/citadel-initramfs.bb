DESCRIPTION = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

SRC_URI = "\
    file://initrd-release \
    file://crypttab \
    file://11-dm.rules \
    file://citadel-lvm-activate.service \
    file://citadel-rootfs-mount.path \
    file://citadel-rootfs-mount.service \
    file://citadel-rootfs-setup.service \
    file://citadel-install-rootfs-mount.service \
    file://citadel-install-rootfs-setup.service \
    file://99-grsec.conf \
"

S = "${WORKDIR}"

dirs755="/boot /dev /usr /usr/bin /usr/sbin /usr/lib /usr/share /etc /proc /run /var /sys /tmp"


do_install() {
    for d in ${dirs755}; do
        install -m 0755 -d ${D}$d
    done

    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-lvm-activate.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-rootfs-mount.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-rootfs-mount.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-rootfs-setup.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-install-rootfs-mount.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/citadel-install-rootfs-setup.service ${D}${systemd_system_unitdir}

    install -d ${D}${systemd_system_unitdir}/sysinit.target.wants
    ln -s ../citadel-rootfs-mount.path ${D}${systemd_system_unitdir}/sysinit.target.wants/citadel-rootfs-mount.path
    ln -s ../citadel-rootfs-setup.service ${D}${systemd_system_unitdir}/sysinit.target.wants/citadel-rootfs-setup.service
    ln -s ../citadel-install-rootfs-mount.service ${D}${systemd_system_unitdir}/sysinit.target.wants/citadel-install-rootfs-mount.service

    install -d ${D}${libdir}/sysctl.d/
    install -m 0644 ${WORKDIR}/99-grsec.conf ${D}${libdir}/sysctl.d/

    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/initrd-release ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/crypttab ${D}${sysconfdir}
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 644 ${WORKDIR}/11-dm.rules ${D}${sysconfdir}/udev/rules.d

    install -d ${D}/dev
    mknod -m 622 ${D}/dev/console c 5 1
    mknod -m 644 ${D}/dev/loop0 b 7 0
}

FILES_${PN} += "/dev/console /boot /dev /usr /etc /proc /run /sys /tmp"

pkg_postinst_${PN}() {
    ln -sf initrd-release $D${sysconfdir}/os-release
    ln -sf ${systemd_system_unitdir}/initrd.target $D${systemd_system_unitdir}/default.target
    > $D${sysconfdir}/fstab
}

inherit allarch
