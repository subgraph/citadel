# Recipe created by recipetool
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=f70a3617a33510362b75a127f401e607"

SRC_URI = "https://github.com/Airblader/xcb-util-xrm/releases/download/v${PV}/xcb-util-xrm-${PV}.tar.gz"
SRC_URI[md5sum] = "c9c63ac728d19eefbf2319f0dad4c127"
SRC_URI[sha256sum] = "c4a1d64d4a6973c649e3b6e3c418242f11e39c65d4d227d555d48f6df0558567"

DEPENDS = "libx11 xcb-util"

inherit pkgconfig autotools

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = ""

