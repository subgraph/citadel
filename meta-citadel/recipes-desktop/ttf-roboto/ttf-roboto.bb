SUMMARY = "Roboto font"
HOMEPAGE = "https://fonts.google.com/specimen/Roboto"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "https://github.com/google/roboto/releases/download/v2.136/roboto-hinted.zip"
SRC_URI[md5sum] = "b796348e23f38be21c84b9bc64c04046"
SRC_URI[sha256sum] = "c4528791de55ade3d6c698738a70b457311e4dd296c5a3318aa729874067fa6a"

inherit allarch fontcache

S = "${WORKDIR}/roboto-hinted"

FILES_${PN} = "${datadir}/fonts/TTF"

do_install () {
    install -Dm644 ${S}/*.ttf -t ${D}${datadir}/fonts/TTF
}

