
inherit meson pkgconfig


LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7578fad101710ea2d289ff5411f1b818"

SRC_URI = "gitsm://github.com/swaywm/wlroots \
           file://0001-Reverting-meson-version-to-match-what-we-use.patch \
           "
PV = "0.1.0+git${SRCPV}"
PR = "r0"

UPSTREAM_CHECK_COMMITS = "1"

SRCREV = "be6210cf8216c08a91e085dac0ec11d0e34fb217"

S = "${WORKDIR}/git"

DEPENDS = "libx11 libdrm dbus libxcb xcb-util-wm xcb-util-image virtual/egl mesa wayland wayland-native libxkbcommon libinput systemd pixman"


