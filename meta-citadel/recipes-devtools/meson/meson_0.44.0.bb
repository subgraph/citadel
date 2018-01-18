HOMEPAGE = "http://mesonbuild.com"
SUMMARY = "A high performance build system"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI = "git://github.com/mesonbuild/meson.git \
           file://0001-use-exe-wrapper-for-custom-targets.patch \
           "

SRCREV = "e674434389249d9b65e6403eb608e4c33d1d48cb"

S = "${WORKDIR}/git"

inherit setuptools3

RDEPENDS_${PN} = "ninja python3-core python3-modules"

BBCLASSEXTEND = "native"
