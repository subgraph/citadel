SUMMARY = "Library for password quality checking and generating random passwords"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bd2f1386df813a459a0c34fde676fc2"

SRC_URI = "https://github.com/libpwquality/libpwquality/releases/download/libpwquality-${PV}/libpwquality-${PV}.tar.bz2"
SRC_URI[md5sum] = "b8defcc7280a90e9400d6689c93a279c"
SRC_URI[sha256sum] = "1de6ff046cf2172d265a2cb6f8da439d894f3e4e8157b056c515515232fade6b"

DEPENDS = "cracklib libpam"

FILES_${PN} += "/usr/lib/security"
FILES_${PN}-staticdev += "${libdir}/security/pam_pwquality.a"

inherit gettext autotools

EXTRA_OECONF = "--disable-python-bindings"

