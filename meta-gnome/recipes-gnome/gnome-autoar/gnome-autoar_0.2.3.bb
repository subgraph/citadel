SUMMARY = "Provides functions, widgets, and gschemas for GNOME applications which want to use archives to transfer directories over the internet"
HOMEPAGE = "https://wiki.gnome.org/TingweiLan/GSoC2013Final"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

SRC_URI[archive.md5sum] = "39b13fb2fc7752fa3b80616244dc4ef8"
SRC_URI[archive.sha256sum] = "5de9db0db028cd6cab7c2fec46ba90965474ecf9cd68cfd681a6488cf1fb240a"

DEPENDS = "glib-2.0 gtk+3 glib-2.0-native gnome-common autoconf-archive libarchive"

do_compile_prepend() {
       export GIR_EXTRA_LIBS_PATH="${B}/gnome-autoar/.libs"
}

inherit gnomebase gobject-introspection vala 

EXTRA_OECONF = ""

