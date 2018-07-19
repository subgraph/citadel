
inherit meson pkgconfig

SRC_URI[md5sum] = "faea394d9c496510096a04fe5430582c"
SRC_URI[sha256sum] = "c58ec15558debbb33e3390250e38375bc80b2275c8e902680424f7837bffabb3"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e316e9609dd7672b87ff25b46b2cf3e1"

SRC_URI = "gitsm://github.com/emersion/grim" 
PV = "0.1+git${SRCPV}"
PR = "r0"

UPSTREAM_CHECK_COMMITS = "1" 

SRCREV = "caf66bc67fb0c9206aac486b9fe3074c77b07cc4"

S = "${WORKDIR}/git"

DEPENDS = "wayland wayland-native wayland-protocols cairo"


