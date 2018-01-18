SUMMARY = "Low level configuration database backend for GSettings"
HOMEPAGE = "https://wiki.gnome.org/Projects/dconf"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=2d5025d4aa3495befef8f17206a5b0a1"

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/dconf/0.26/dconf-${PV}.tar.xz"
SRC_URI[md5sum] = "a3cb67032e060450fa01c1a0f874bb60"
SRC_URI[sha256sum] = "d583b1f7fc93b879e2956acc6a26ea05a445a0002158aeef80c8e378e1414535"

DEPENDS = "glib-2.0 xmlto-native glib-2.0-native"

FILES_${PN} += "\
    ${libdir}/gio/modules/libdconfsettings.so \
    ${datadir}/bash-completion/completions/dconf \
    ${datadir}/vala/vapi \
    ${datadir}/dbus-1/services \
"

inherit pkgconfig gettext autotools

EXTRA_OECONF = "--enable-man=no"

BBCLASSEXTEND= "native"
