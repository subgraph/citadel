SUMMARY = "A C/C++ implementation of a Sass compiler"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f34396ca205f5e119ee77aae91fa27d \
                    file://LICENSE;md5=2f8a76980411a3f1f1480b141ce06744"

SRC_URI = "https://github.com/sass/libsass/archive/${PV}.tar.gz;downloadfilename=${PN}-${PV}.tar.gz"
SRC_URI[md5sum] = "dcac228e89511d25ea926aa3d98d7b44"
SRC_URI[sha256sum] = "855c40528b897d06ae4d24606c2db3cd09bb38de5b46b28e835f9d4fd4d7ab95"

inherit autotools

EXTRA_OECONF = ""
BBCLASSEXTEND = "native"

