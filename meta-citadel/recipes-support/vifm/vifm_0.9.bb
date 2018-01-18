LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263" 

SRC_URI = "https://github.com/vifm/vifm/releases/download/v${PV}/vifm-${PV}.tar.bz2 \
           file://0001-prefix-needed-for-ncurses-check.patch \
           "
SRC_URI[md5sum] = "a523a16a7a0170141b5a36cb67251490"
SRC_URI[sha256sum] = "ab10c99d1e4c24ff8a03c20be1c202cc15874750cc47a1614e6fe4f8d816a7fd"

DEPENDS = "file ncurses libx11 groff-native"
RDEPENDS_${PN} = "ncurses-terminfo"

FILES_${PN} += "\
    ${datadir}/bash-completion \
"

do_install_append() {
    rm -rf ${D}${datadir}/zsh
}
inherit perlnative autotools

