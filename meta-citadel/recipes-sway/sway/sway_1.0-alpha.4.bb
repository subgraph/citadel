LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dfc67e5b1fa10ebb4b70eb0c0ca67bea"

SRC_URI = "https://github.com/swaywm/sway/archive/${PV}.tar.gz \
		   file://config \
		   "

SRC_URI[md5sum] = "1010dccf22236de3a353eaa938f5a2cf"
SRC_URI[sha256sum] = "c381ffc6a91037642e9d2205a9bfce3c4c120dfcc4d649d190e4ac7b74dcf166"

DEPENDS = "dbus cairo pango wlroots libinput libxkbcommon wayland wayland-native libpam libcap json-c libpcre gdk-pixbuf"

inherit meson 

FILES_${PN} += "\
	${datadir}/wayland-sessions/sway.desktop \
"

do_install_append() {
    rm ${D}${sysconfdir}/sway/config
    install -m 644 ${WORKDIR}/config ${D}${sysconfdir}/sway/config
}

EXTRA_OEMESON += "-Ddefault_wallpaper=false -Dzsh_completions=false" 

