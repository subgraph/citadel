SUMMARY = "Provides functions, widgets, and gschemas for GNOME applications which want to use archives to transfer directories over the internet"
HOMEPAGE = "https://wiki.gnome.org/TingweiLan/GSoC2013Final"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

SRC_URI[archive.md5sum] = "90b4980c96614bcc376af44717deef99"
SRC_URI[archive.sha256sum] = "e1fe2c06eed30305c38bf0939c72b0e51b4716658e2663a0cf4a4bf57874ca62"

DEPENDS = "glib-2.0 gtk+3 glib-2.0-native gnome-common autoconf-archive libarchive"

do_compile_prepend() {
       export GIR_EXTRA_LIBS_PATH="${B}/gnome-autoar/.libs"
}

inherit gnomebase gobject-introspection vala 

EXTRA_OECONF = ""

