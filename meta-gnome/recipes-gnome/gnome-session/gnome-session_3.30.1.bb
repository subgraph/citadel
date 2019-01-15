SUMMARY = "GNOME session management"
HOMEPAGE = "https://wiki.gnome.org/Projects/SessionManagement"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase meson 

SRC_URI[archive.md5sum] = "45c33dfaad7d40c008f8131aff2e0391"
SRC_URI[archive.sha256sum] = "eafe85972689186c7c6b5fe1d3bb4dc204a1e0e6b6e763e24b8fb43a40c07739"

DEPENDS = "glib-2.0-native intltool-native xmlto-native glib-2.0 gnome-desktop json-glib"
RDEPENDS_${PN} = "gnome-settings-daemon"

FILES_${PN} += "\
    ${datadir}/xsessions \
    ${datadir}/glib-2.0/schemas \
    ${datadir}/GConf \
    ${datadir}/wayland-sessions \
"
EXTRA_OEMESON = "-Denable-docbook=false -Denable-man=false"

