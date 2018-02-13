
hostname="subgraph"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "\
    file://locale.conf \
    file://environment.sh \
    file://fstab \
    file://99-grsec-debootstrap.conf \
"

do_install_append () {
    install -m 0755 -d ${D}/storage
    install -m 0755 -d ${D}/var/lib/machines
    install -m 0755 -d ${D}${sysconfdir}/profile.d
    install -m 0644 ${WORKDIR}/locale.conf ${D}${sysconfdir}/locale.conf
    install -m 0644 ${WORKDIR}/environment.sh ${D}${sysconfdir}/profile.d/environment.sh
    install -m 0644 ${WORKDIR}/fstab ${D}${sysconfdir}/fstab

    # disable some pax and grsecurity features so that debootstrap will work
    # this should be removed later
    install -d ${D}${libdir}/sysctl.d
    install -m 0644 ${WORKDIR}/99-grsec-debootstrap.conf ${D}${libdir}/sysctl.d/
}
