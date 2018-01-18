SUMMARY = "Customize advanced GNOME desktop configuration options"
HOMEPAGE = "https://wiki.gnome.org/action/show/Apps/Tweaks"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSES/GPL-3.0;md5=9eef91148a9b14ec7f9df333daebc746"

SRC_URI = "https://download.gnome.org/sources/gnome-tweak-tool/3.27/gnome-tweak-tool-${PV}.tar.xz"
SRC_URI[md5sum] = "4fb5b7649a663d077acf8282cb2163da"
SRC_URI[sha256sum] = "43b77c256bf97e20d2c1c813736a2d9bc684d2c60c126f758e18566caa0f14fd"

DEPENDS =  "glib-2.0 glib-2.0-native"

RDEPENDS_${PN} = "python3 python3-pygobject"

FILES_${PN} += "\
    ${libdir}/python3.5/site-packages/gtweak \
    ${datadir}/icons/hicolor \
    ${datadir}/metainfo \
"
inherit meson

