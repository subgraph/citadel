DESCRIPTION = ""
HOMEPAGE = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=e6faad78a557b22780e4cf62c89976f8"
SECTION = ""
DEPENDS = ""

SRC_URI = "git://github.com/danielwe/base16-vim;protocol=https"
SRCREV = "a40a4514ce82619e32028d39966a2fa61ebbf7e4"

FILES_${PN} = "${datadir}/vim/colors"
S = "${WORKDIR}/git"

inherit allarch

do_configure () {
    :
}

do_compile() {
    :
}

do_install() {
    mkdir -p ${D}${datadir}/vim/colors
    install -Dm644 ${S}/colors/*.vim ${D}${datadir}/vim/colors
}
