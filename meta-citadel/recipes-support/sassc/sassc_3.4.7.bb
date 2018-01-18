LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2f8a76980411a3f1f1480b141ce06744"

SRC_URI = "https://github.com/sass/sassc/archive/${PV}.tar.gz;downloadfilename=${PN}-${PV}.tar.gz"
SRC_URI[md5sum] = "a3b975e21a6485643f836a9880b1128a"
SRC_URI[sha256sum] = "b3ff3c1741a133440d7ef59ba405d4289ceee36dfde2a49501a7c57d75649900"

DEPENDS = "libsass"

inherit autotools

EXTRA_OECONF = ""

BBCLASSEXTEND = "native"
