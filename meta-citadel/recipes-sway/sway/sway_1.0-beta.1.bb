LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dfc67e5b1fa10ebb4b70eb0c0ca67bea"

SRC_URI = "https://github.com/swaywm/sway/archive/${PV}.tar.gz \
		   file://config \
		   "

SRC_URI[md5sum] = "680ed07c0ce401af645a99e42ea459be"
SRC_URI[sha256sum] = "382955b9373219b2be6e2899a9f204383fe7ab9808ee28e56735b431bfbd81e2"

DEPENDS = "dbus cairo pango wlroots libinput libxkbcommon wayland wayland-native libpam libcap json-c libpcre gdk-pixbuf"

inherit meson 

FILES_${PN} += "\
	${datadir}/wayland-sessions/sway.desktop \
"

do_install_append() {
    rm ${D}${sysconfdir}/sway/config
    install -m 644 ${WORKDIR}/config ${D}${sysconfdir}/sway/config
}

EXTRA_OEMESON += "-Ddefault-wallpaper=false -Dzsh-completions=false -Dbash-completions=false -Dfish-completions=false" 

