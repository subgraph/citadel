SUMMARY = "Default GNOME desktop background images"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=75859989545e37968a99b631ef42722e"

SRC_URI[archive.md5sum] = "eca3276373841a0cec2ed582d52a5899"
SRC_URI[archive.sha256sum] = "b25b963d9d1ce076b489ef1e85c6540166f2312c77132f4ec0ecc90f3da8f1e1"


FILES_${PN} += "\
    ${datadir}/backgrounds/gnome \
    ${datadir}/gnome-background-properties \
"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext


