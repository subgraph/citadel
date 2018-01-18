
hostname="citadel"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "\
    file://locale.conf \
    file://environment.sh \
    file://fstab \
"

do_install_append () {
    install -m 0755 -d ${D}/storage
    install -m 0755 -d ${D}${sysconfdir}/profile.d
    install -m 0644 ${WORKDIR}/locale.conf ${D}${sysconfdir}/locale.conf
    install -m 0644 ${WORKDIR}/environment.sh ${D}${sysconfdir}/profile.d/environment.sh
    install -m 0644 ${WORKDIR}/fstab ${D}${sysconfdir}/fstab
}
