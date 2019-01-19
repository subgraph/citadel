SUMMARY = "GNOME Screenshot"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext gobject-introspection

DEPENDS += "libx11 libxext glib-2.0 gtk+3 libcanberra glib-2.0-native libxml2-native"

SRC_URI += "file://0001-Don-t-process-or-install-appstream-file.patch"

SRC_URI[archive.md5sum] = "39a4ba8ef7b6c657a951f0822d218a34"
SRC_URI[archive.sha256sum] = "88031ec38ea823d97ddd6884c9b831acc2b33e2d3fd71112c94a1bdcb71ebd19"

FILES_${PN} += "${datadir}/dbus-1/services"

