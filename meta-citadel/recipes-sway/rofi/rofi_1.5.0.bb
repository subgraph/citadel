# Recipe created by recipetool
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=e326af0896de95c64d15bde1a2704cd1"

SRC_URI = "https://github.com/DaveDavenport/rofi/releases/download/${PV}/rofi-${PV}.tar.gz"
SRC_URI[md5sum] = "a60fa45011dc2803533ba7a5dca4c5b6"
SRC_URI[sha256sum] = "541de71ffe115951aeb3abe732c99d579f7a8711908e2768accf58e030470640"

DEPENDS = "cairo pango libcheck librsvg flex-native startup-notification bison-native glib-2.0-native libxkbcommon xcb-util xcb-util-wm xcb-util-image xcb-util-xrm"

# NOTE: if this software is not capable of being built in a separate build directory
# from the source, you should replace autotools with autotools-brokensep in the
# inherit line
inherit pkgconfig autotools

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF += "--enable-drun"

