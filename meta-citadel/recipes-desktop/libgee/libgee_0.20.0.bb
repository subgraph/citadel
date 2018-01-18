SUMMARY = "A collection library providing GObject based interfaces and classes for commonly used data structures"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/libgee/0.20/libgee-${PV}.tar.xz"
SRC_URI[md5sum] = "66a4bfb6d7b03248acb99d140aac127d"
SRC_URI[sha256sum] = "21308ba3ed77646dda2e724c0e8d5a2f8d101fb05e078975a532d7887223c2bb"

DEPENDS = "glib-2.0"

PACKAGES += "${PN}-vala"
FILES_${PN}-vala = "${datadir}/vala/vapi"

inherit pkgconfig autotools

EXTRA_OECONF = ""

