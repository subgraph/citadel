SUMMARY = "A collection library providing GObject based interfaces and classes for commonly used data structures"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/libgee/0.20/libgee-${PV}.tar.xz"
SRC_URI[md5sum] = "d224dca55bb909f6730f40cc267337be"
SRC_URI[sha256sum] = "bb2802d29a518e8c6d2992884691f06ccfcc25792a5686178575c7111fea4630"


DEPENDS = "glib-2.0"

PACKAGES += "${PN}-vala"
FILES_${PN}-vala = "${datadir}/vala/vapi"

inherit pkgconfig autotools

EXTRA_OECONF = ""

