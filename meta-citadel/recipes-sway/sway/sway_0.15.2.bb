# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dfc67e5b1fa10ebb4b70eb0c0ca67bea"

SRC_URI = "https://github.com/swaywm/sway/archive/${PV}.tar.gz \
		   file://config \
		   "

SRC_URI[md5sum] = "b89e6ea3a786eacac825a91ff4545491"
SRC_URI[sha256sum] = "05526e3038d2a5490a64bd816f1f04d2a6c214ddc6182835312b273b40b737ae"

DEPENDS = "dbus cairo pango wlc libinput libxkbcommon wayland wayland-native libpam libcap json-c libpcre gdk-pixbuf"

inherit cmake

FILES_${PN} += "\
	${datadir}/wayland-sessions/sway.desktop \
"

do_install_append() {
    rm ${D}${sysconfdir}/sway/config
    install -m 644 ${WORKDIR}/config ${D}${sysconfdir}/sway/config
}

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE += "-Ddefault-wallpaper=no -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_SYSCONFDIR=/etc"

