SUMMARY = "Location and timezone database and weather lookup library"
HOMEPAGE = "https://wiki.gnome.org/Projects/LibGWeather"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI = "https://download.gnome.org/sources/libgweather/3.26/libgweather-${PV}.tar.xz"
SRC_URI[md5sum] = "798098eddb2cd4a7f582a3f9aeb1b08d"
SRC_URI[sha256sum] = "5b84badc0b3ecffff5db1bb9a7cc4dd4e400a8eb3f1282348f8ee6ba33626b6e"

DEPENDS = "gtk+3 libxml2 libsoup-2.4 glib-2.0 intltool-native geocode-glib glib-2.0-native"

FILES_${PN} += "${datadir}/glib-2.0/schemas"

inherit pythonnative pkgconfig gettext autotools gobject-introspection

EXTRA_OECONF = "--enable-introspection=yes --enable-vala=no --enable-glade-catalog=no --disable-glibtest"

