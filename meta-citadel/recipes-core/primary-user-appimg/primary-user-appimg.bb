
DESCRIPTION = "Install systemd unit file to automatically start primary-user-appimg container"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

inherit systemd

SRC_URI = "file://primary-user-appimg.path file://primary-user-appimg.service file://primary.nspawn file://watch-run-user.path file://watch-run-user.service file://run-in-image"

S = "${WORKDIR}"

SYSTEMD_SERVICE_${PN} = "watch-run-user.path"
RDEPENDS_${PN} = "bash"

FILES_${PN} += "\
    ${systemd_system_unitdir}/watch-run-user.service \
    ${systemd_system_unitdir}/primary-user-appimg.path \
    ${systemd_system_unitdir}/primary-user-appimg.service \
"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/primary-user-appimg.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/primary-user-appimg.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/watch-run-user.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/watch-run-user.service ${D}${systemd_system_unitdir}
    install -d ${D}${sysconfdir}/systemd/nspawn
    install -m 644 ${WORKDIR}/primary.nspawn ${D}${sysconfdir}/systemd/nspawn
    install -d ${D}${libexecdir}
    install -m 755 ${WORKDIR}/run-in-image ${D}${libexecdir}
}
