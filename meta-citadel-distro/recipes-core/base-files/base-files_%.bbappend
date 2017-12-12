
hostname="citadel"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://locale.conf"

do_install_append () {
    install -m 0755 -d ${D}/storage
    install -m 0655 ${WORKDIR}/locale.conf ${D}${sysconfdir}/locale.conf
}
