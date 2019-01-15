LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d791728a073bc009b4ffaf00b7599855"

SRC_URI = "git://github.com/jderose9/dash-to-panel.git;protocol=https"
SRCREV="035239ae692b616271bafa86172e2a6e55393974"
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

