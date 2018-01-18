SUMMARY = "Library to identify and configure Wacom tablets"
HOMEPAGE = "http://linuxwacom.sourceforge.net/wiki/index.php/Libwacom"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=40a21fffb367c82f39fd91a3b137c36e"

SRC_URI = "https://downloads.sourceforge.net/linuxwacom/libwacom-${PV}.tar.bz2"
SRC_URI[md5sum] = "00d7f50bc7feda6a01f2b2546f787bc9"
SRC_URI[sha256sum] = "c3e4109c8aa675ca42cafbf39992dcd1fd6582314441c42ba32b49f5b79cfb76"

DEPENDS = "libgudev libxml2 glib-2.0"

inherit pkgconfig autotools

EXTRA_OECONF = ""

