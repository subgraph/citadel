SUMMARY = "An implementation of XDG Sound Theme and Name specification for generating sound events on free desktops"
HOMEPAGE = "http://0pointer.de/lennart/projects/libcanberra"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LGPL;md5=2d5025d4aa3495befef8f17206a5b0a1"

SRC_URI = "http://0pointer.de/lennart/projects/libcanberra/libcanberra-${PV}.tar.xz"
SRC_URI[md5sum] = "34cb7e4430afaf6f447c4ebdb9b42072"
SRC_URI[sha256sum] = "c2b671e67e0c288a69fc33dc1b6f1b534d07882c2aceed37004bf48c601afa72"

DEPENDS = "libtool glib-2.0 alsa-lib libvorbis systemd gtk+3"

FILES_${PN} += "\
    ${libdir}/${PN}-${PV} \
    ${datadir}/gnome \
    ${datadir}/gdm \
    ${libdir}/gtk-3.0 \
"
FILES_${PN}-dev += "\
    ${libdir}/gnome-settings-daemon-3.0 \
    ${libdir}/gtk-3.0/modules/libcanberra-gtk-module.so \
"

do_install_append () {
    rm -rf ${D}/${datadir}/vala
}

inherit pkgconfig autotools

EXTRA_OECONF = "--disable-oss --disable-pulse --disable-gstreamer --disable-tdb --disable-lynx --disable-gtk-doc --disable-udev"

