SUMMARY = "Customize advanced GNOME desktop configuration options"
HOMEPAGE = "https://wiki.gnome.org/action/show/Apps/Tweaks"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSES/GPL-3.0;md5=9eef91148a9b14ec7f9df333daebc746"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext

SRC_URI[archive.md5sum] = "7fd38ddd8ed2233494e67706599c21a0"
SRC_URI[archive.sha256sum] = "2944532de25cd41631afe1b0d154a6b2377551c1c67fffa5a1c4928c94eb1f35"

DEPENDS =  "glib-2.0 glib-2.0-native"

RDEPENDS_${PN} = "python3 python3-pygobject"

FILES_${PN} += "\
    ${libdir}/python3.5/site-packages/gtweak \
    ${datadir}/icons/hicolor \
    ${datadir}/metainfo \
"

