DESCRIPTION = "GNU FriBidi"
HOMEPAGE = "https://github.com/fribidi"

DEPENDS = ""

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=a916467b91076e631dd8edb7424769c7"

SRCREV = "f2c9d50722cb60d0cdec3b1bafba9029770e86b4"
SRC_URI = "git://github.com/fribidi/fribidi.git;protocol=https"

S = "${WORKDIR}/git"

inherit autotools

EXTRA_OECONF = "--disable-deprecated --disable-docs"

BBCLASSEXTEND = "native"
