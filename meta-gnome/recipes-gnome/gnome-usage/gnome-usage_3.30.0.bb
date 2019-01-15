SUMMARY = "GNOME Usage"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext vala

SRC_URI += "file://0001-Added-machine-tags.patch"

DEPENDS = "glib-2.0 glib-2.0-native gtk+3 gobject-introspection libgtop"
SRC_URI[archive.md5sum] = "7641fecd816f76d31415f4e2e2b301b6"
SRC_URI[archive.sha256sum] = "50577b76d05310cb6f26138fb6f4d0c8d02d4f71a657ac3f445f999038633b38"


FILES_${PN} += "${datadir}/metainfo"

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

