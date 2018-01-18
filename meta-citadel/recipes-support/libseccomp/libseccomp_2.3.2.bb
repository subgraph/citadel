LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7c13b3376cea0ce68d2d2da0a1b3a72c"

SRC_URI = "https://github.com/seccomp/libseccomp/releases/download/v${PV}/libseccomp-${PV}.tar.gz"
SRC_URI[md5sum] = "e74a626bea0cd607c23229b10b5f93da"
SRC_URI[sha256sum] = "3ddc8c037956c0a5ac19664ece4194743f59e1ccd4adde848f4f0dae7f77bca1"

inherit autotools

EXTRA_OECONF = ""

