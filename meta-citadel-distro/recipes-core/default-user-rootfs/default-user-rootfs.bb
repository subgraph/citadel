
DESCRIPTION = "Install systemd unit file to automatically start default-user-rootfs container"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

inherit systemd

SRC_URI = "file://default-user-rootfs.service"

S = "${WORKDIR}"

SYSTEMD_SERVICE_${PN} = "default-user-rootfs.service"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/default-user-rootfs.service ${D}${systemd_system_unitdir}
}
