HOMEPAGE = "https://snwh.org/paper"
LICENSE = "CC-BY-SA-4.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=8335471e369a410f354d5179fdcf0195 \
                    file://LICENSE;md5=4a312f184ff2ceafe9466cbb7ea43f4e"

SRC_URI = "https://github.com/snwh/paper-icon-theme/archive/v1.4.0.tar.gz;downloadfilename=paper-icon-theme-${PV}.tar.gz"
SRC_URI[md5sum] = "076e1dfa1bc5928f4c6616ffd933926a"
SRC_URI[sha256sum] = "b90f3a84634572bcba76cdd0c2a0d305a5c521c2054d3d390edffda5f233928b"

S = "${WORKDIR}/${BPN}-1.4.0"

inherit autotools-brokensep allarch 

FILES_${PN} = "${datadir}/icons"

EXTRA_OECONF = ""

