LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7c13b3376cea0ce68d2d2da0a1b3a72c"

SRCREV = "74b190e1aa05f07da0c61fb9a30dbc9c18ce2c9d"
SRC_URI = "git://github.com/seccomp/libseccomp.git;protocol=https;branch=release-2.3"

S = "${WORKDIR}/git"

inherit autotools

EXTRA_OECONF = ""

