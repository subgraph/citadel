SUMMARY = "GTK support library for colord"
HOMEPAGE = "https://www.freedesktop.org/software/colord/"

LICENSE = "LGPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=e6a600fd5e1d9cbde2d983680233ad02"

SRC_URI = "https://www.freedesktop.org/software/colord/releases/colord-gtk-${PV}.tar.xz"
SRC_URI[md5sum] = "bb9d6f3c037152ad791003375aa6c16c"
SRC_URI[sha256sum] = "28d00b7f157ea3e2ea5315387b2660fde82faba16674861c50465e55d61a3e45"

DEPENDS = "lcms glib-2.0 intltool-native gtk+3 glib-2.0-native colord"

inherit pkgconfig gettext autotools

EXTRA_OECONF = "--enable-introspection=no --disable-gtk-doc"

