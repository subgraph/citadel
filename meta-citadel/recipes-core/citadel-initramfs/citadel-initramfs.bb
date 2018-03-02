DESCRIPTION = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

SRC_URI = "file://initrd-release file://crypttab file://11-dm.rules"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/initrd-release ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/crypttab ${D}${sysconfdir}
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 644 ${WORKDIR}/11-dm.rules ${D}${sysconfdir}/udev/rules.d
    install -d ${D}/dev
    mknod -m 622 ${D}/dev/console c 5 1
}

FILES_${PN} += "/dev/console"

pkg_postinst_${PN}() {
    ln -sf initrd-release $D${sysconfdir}/os-release
    #ln -s ${systemd_unitdir}/systemd $D/init
    ln -sf ${systemd_system_unitdir}/initrd.target $D${systemd_system_unitdir}/default.target
    rm -f $D${sysconfdir}/fstab
    > $D${sysconfdir}/fstab
}

inherit allarch
