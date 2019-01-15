# Append recipe from meta-openembedded/meta-networking
PACKAGECONFIG = "nss dhclient systemd wifi glib"
EXTRA_OECONF_remove = "--with-nmtui=yes"
EXTRA_OECONF += "--disable-ovs --with-nmtui=no"

SRC_URI += "\
    file://watch-resolvconf.path \
    file://watch-resolvconf.service \
"

SYSTEMD_SERVICE_${PN} += "watch-resolvconf.path"

do_install_append() {
    install -m 644 ${WORKDIR}/watch-resolvconf.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/watch-resolvconf.service ${D}${systemd_system_unitdir}
}
