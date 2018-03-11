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

SRC_URI[md5sum] = "ea770561a77ad3d6138f44673d491250"
SRC_URI[sha256sum] = "b4305581587b1072f5cb61387230aaf731ea9ea0d01fdf198856a757e2195149"

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

