SUMMARY = "Library for accessing information about running processes"
HOMEPAGE = "https://developer.gnome.org/libgtop/stable/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552" 

SRC_URI[archive.md5sum] = "bb0ce7de6b28694b40405eedac8a31b5"
SRC_URI[archive.sha256sum] = "4f6c0e62bb438abfd16b4559cd2eca0251de19e291c888cdc4dc88e5ffebb612"

DEPENDS = "libx11 glib-2.0 libxau"

inherit gnomebase perlnative gettext pkgconfig autotools

EXTRA_OECONF = "--with-examples=no"

