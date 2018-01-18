SUMMARY = "Library providing high-level API for X Keyboard Extension"
HOMEPAGE = "https://www.freedesktop.org/wiki/Software/LibXklavier"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=6e29c688d912da12b66b73e32b03d812"

SRC_URI = "https://people.freedesktop.org/~svu/libxklavier-${PV}.tar.bz2"
SRC_URI[md5sum] = "13af74dcb6011ecedf1e3ed122bd31fa"
SRC_URI[sha256sum] = "17a34194df5cbcd3b7bfd0f561d95d1f723aa1c87fca56bc2c209514460a9320"

DEPENDS = "glib-2.0 libx11 libxml2 libxi iso-codes glib-2.0-native"

inherit gettext pkgconfig autotools

EXTRA_OECONF = ""

