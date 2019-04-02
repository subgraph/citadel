DESCRIPTION = "A filtering proxy for D-Bus connections"
SUMMARY = "A filtering proxy for D-Bus connections"
HOMEPAGE = "https://github.com/flatpak/xdg-dbus-proxy"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

DEPENDS = "autoconf-archive-native glib-2.0"

inherit autotools gnomebase

SRCREV = "7531b44424f834f193fe7f83fe572084d9c77ad9"
SRC_URI = "git://github.com/flatpak/xdg-dbus-proxy.git;protocol=https" 
S = "${WORKDIR}/git"


