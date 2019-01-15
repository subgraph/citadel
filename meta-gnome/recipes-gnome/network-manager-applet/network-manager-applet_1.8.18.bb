SUMMARY = "Applet for managing network connections"
HOMEPAGE = "https://wiki.gnome.org/Projects/NetworkManager"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"

DEPENDS = "gcr iso-codes networkmanager libgudev gtk+3 intltool-native modemmanager glib-2.0 libnotify libsecret glib-2.0-native"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext gobject-introspection

SRC_URI[archive.md5sum] = "34923579b39360db64649342ee6735d8"
SRC_URI[archive.sha256sum] = "23dc1404f1e0622b7c4718b6d978b101d5e4d9be0b92133b3863a4dc29786178"

FILES_${PN} += "${datadir}/metainfo"

EXTRA_OEMESON = "-Dwwan=false -Dteam=false -Dgcr=false -Dmobile_broadband_provider_info=false -Dgtk_doc=false -Dintrospection=true -Dselinux=false"
