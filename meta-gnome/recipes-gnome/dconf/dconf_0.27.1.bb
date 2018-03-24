SUMMARY = "Low level configuration database backend for GSettings"
HOMEPAGE = "https://wiki.gnome.org/Projects/dconf"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=2d5025d4aa3495befef8f17206a5b0a1"

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/dconf/0.27/dconf-${PV}.tar.xz"
SRC_URI[archive.md5sum] = "30bb3010ecd36d6e53ddbc40ef4b80ec"
SRC_URI[archive.sha256sum] = "37daf52e68d03ca0b6d0c2e0df7acac64e091074457ae306683b78b5cfa7e9ce"

DEPENDS = "glib-2.0 xmlto-native glib-2.0-native"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext vala

FILES_${PN} += "\
    ${libdir}/gio/modules/libdconfsettings.so \
    ${datadir}/bash-completion/completions/dconf \
    ${datadir}/vala/vapi \
    ${datadir}/dbus-1/services \
"

BBCLASSEXTEND= "native"
