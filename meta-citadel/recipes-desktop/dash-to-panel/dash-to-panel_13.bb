LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d791728a073bc009b4ffaf00b7599855"

SRC_URI = "git://github.com/jderose9/dash-to-panel.git;protocol=https"
#SRC_URI[md5sum] = "63bd7ce2b4907903fead14320f9d7f52"
#SRC_URI[sha256sum] = "e4c5ef2e9f2c69ee3fe74bf526f4bc5c3991f78aa96b1732c70712544981bfa6"
SRCREV="a460d2828ac38b27b7688115910f6eb6b9f274a3"
S = "${WORKDIR}/git"

DEPENDS = "gettext-native glib-2.0-native"
FILES_${PN} = "${datadir}/gnome-shell/extensions"

do_configure () {
	:
}

do_compile () {
	oe_runmake
}

do_install () {
	oe_runmake install 'DESTDIR=${D}'
}

