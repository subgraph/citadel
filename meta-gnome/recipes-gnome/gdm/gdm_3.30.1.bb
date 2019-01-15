SUMMARY = "GNOME display manager"
HOMEPAGE = "https://wiki.gnome.org/Projects/GDM"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit gnomebase useradd gettext pkgconfig autotools gobject-introspection systemd

SRC_URI[archive.md5sum] = "83094013e224a072f6adc086c034f076"
SRC_URI[archive.sha256sum] = "4b3d11856adf9fc26b43b2742c196e9e9dc2d2a5eff8bb34d906537288e2732f"

SRC_URI += "\
    file://gdm.conf \
    file://0001-replace-absolute-path-with-staging-path.patch \
"

DEPENDS = "libxcb libxinerama libxau libxdmcp intltool-native glib-2.0 accountsservice libpam libcheck libcanberra gtk+3 libx11 libxi systemd libxext glib-2.0-native dconf-native plymouth"

FILES_${PN} += "\
    ${libdir}/systemd/system/gdm.service \
    ${libdir}/security/pam_gdm.so \
    ${datadir}/gnome-session/sessions \
    ${datadir}/dconf/profile \
    /run/gdm/greeter \
"

do_install_append() {
    install -d ${D}${sysconfdir}/default/volatiles
    echo "d gdm gdm 755 ${localstatedir}/run/gdm/greeter none" > ${D}${sysconfdir}/default/volatiles/99_gdm
    rm -rf ${D}/run/gdm
    rm ${D}${sysconfdir}/gdm/custom.conf
    rm ${D}${libdir}/udev/rules.d/61-gdm.rules
    install -m 644 ${WORKDIR}/gdm.conf ${D}${sysconfdir}/gdm/custom.conf
}


SYSTEMD_SERVICE_${PN} = "gdm.service"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "--system --home /var/lib/gdm -u 21 -s /bin/false --user-group gdm"

EXTRA_OECONF = "--enable-introspection --enable-systemd-journal --enable-wayland-support --with-xdmcp=no --disable-static --with-default-pam-config=openembedded --with-plymouth=yes"

