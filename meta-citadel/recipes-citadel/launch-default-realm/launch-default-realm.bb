
DESCRIPTION = "Install systemd unit file to automatically start default realm"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

inherit systemd

SRC_URI = "file://launch-default-realm.path file://launch-default-realm.service file://watch-run-user.path file://watch-run-user.service" 

S = "${WORKDIR}"

SYSTEMD_SERVICE_${PN} = "watch-run-user.path"
RDEPENDS_${PN} = "bash"

FILES_${PN} += "\
    ${systemd_system_unitdir}/watch-run-user.service \
    ${systemd_system_unitdir}/launch-default-realm.path \
    ${systemd_system_unitdir}/launch-default-realm.service \
"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/launch-default-realm.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/launch-default-realm.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/watch-run-user.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/watch-run-user.service ${D}${systemd_system_unitdir}
}
