SUMMARY = "A C/C++ implementation of a Sass compiler"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f34396ca205f5e119ee77aae91fa27d \
                    file://LICENSE;md5=2f8a76980411a3f1f1480b141ce06744"

SRC_URI = "git://github.com/sass/libsass.git;protocol=git;branch=3.5-stable"
SRCREV = "39e30874b9a5dd6a802c20e8b0470ba44eeba929"

S = "${WORKDIR}/git"

inherit autotools

EXTRA_OECONF = ""
BBCLASSEXTEND = "native"

