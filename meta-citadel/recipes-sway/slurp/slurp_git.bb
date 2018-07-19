
inherit meson pkgconfig

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e316e9609dd7672b87ff25b46b2cf3e1"

SRC_URI = "gitsm://github.com/emersion/slurp" 
PV = "0.1+git${SRCPV}"
PR = "r0"

UPSTREAM_CHECK_COMMITS = "1"

SRCREV = "a94273e02a5b70ca92dc67a08486198552583818"

S = "${WORKDIR}/git"

DEPENDS = "wayland wayland-native wayland-protocols cairo"


