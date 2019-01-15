SUMMARY = "Default GNOME desktop background images"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=75859989545e37968a99b631ef42722e"

SRC_URI[archive.md5sum] = "13ecd0e4bb4721a68310948e67dbaaaa"
SRC_URI[archive.sha256sum] = "ece63a2aaf2e9b685721d125b7832fee63749db58743bc147ee92e136896e984"

FILES_${PN} += "\
    ${datadir}/backgrounds/gnome \
    ${datadir}/gnome-background-properties \
"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext
