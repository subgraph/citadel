
inherit meson pkgconfig


LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7578fad101710ea2d289ff5411f1b818"

SRC_URI = "gitsm://github.com/swaywm/wlroots"
PV = "0.1+git${SRCPV}"
PR = "r0"

UPSTREAM_CHECK_COMMITS = "1"

SRCREV = "2a58d4467f83c5660bbee6733a73cc1ed92ca478"

S = "${WORKDIR}/git"

DEPENDS = "libx11 libdrm dbus libxcb xcb-util-wm xcb-util-image virtual/egl mesa wayland wayland-native libxkbcommon libinput systemd pixman"


