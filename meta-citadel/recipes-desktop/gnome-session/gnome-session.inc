SUMMARY = "GNOME session management"
HOMEPAGE = "https://wiki.gnome.org/Projects/SessionManagement"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

def gnome_verdir(v):
    return oe.utils.trim_version(v, 2)

GNOMEBN ?= "${BPN}"
SRC_URI = "${GNOME_MIRROR}/${GNOMEBN}/${@gnome_verdir("${PV}")}/${GNOMEBN}-${PV}.tar.xz;name=archive"

DEPENDS = "glib-2.0-native intltool-native xmlto-native glib-2.0 gnome-desktop json-glib"
RDEPENDS_${PN} = "gnome-settings-daemon"
FILES_${PN} += "\
    ${datadir}/xsessions \
    ${datadir}/glib-2.0/schemas \
    ${datadir}/GConf \
    ${datadir}/wayland-sessions \
"
