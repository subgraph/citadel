SUMMARY = "GNOME Usage"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext vala

SRC_URI += "file://0001-Added-machine-tags.patch"

DEPENDS = "glib-2.0 glib-2.0-native gtk+3 gobject-introspection libgtop"
SRC_URI[archive.md5sum] = "f234a0cb0cbce809d00584d45ab5d46a"
SRC_URI[archive.sha256sum] = "d812655c23a59990045f8f282bcd2b138d594b6cd670aaec01e3cf6b235f6004"

FILES_${PN} += "${datadir}/metainfo"

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

