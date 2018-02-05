
DESCRIPTION = ""
HOMEPAGE = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

SRC_URI = "file://default-user-rootfs.service"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${sysconfdir}/systemd/system
    install -m 644 ${WORKDIR}/default-user-rootfs.service ${D}${sysconfdir}/systemd/system
}

pkt_postinst_${PN}() {
    systemctl --root=$D enable default-user-rootfs
}
