SUMMARY = "Tools to manipulate UEFI variables"
DESCRIPTION = "efivar provides a simple command line interface to the UEFI variable facility"
HOMEPAGE = "https://github.com/rhinstaller/efivar"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=6626bb1e20189cfa95f2c508ba286393"

PV = "36"
SRC_NAME = "efivar"
SRC_URI = "https://github.com/rhboot/efivar/releases/download/${PV}/${SRC_NAME}-${PV}.tar.bz2"
SRC_URI[md5sum] = "e98140ab7105e90059dc57a67c8c07e9"
SRC_URI[sha256sum] = "94bfccc20889440978a85f08d5af4619040ee199001b62588d47d676f58c0d33"

S = "${WORKDIR}/${SRC_NAME}-${PV}"

#inherit autotools pkgconfig
inherit pkgconfig autotools

do_configure () {
	:
}

do_compile () {
	cd ${S}/src
	make
	:
#oe_runmake
}

#do_install () {
#	oe_runmake install DESTDIR=${D}
#}

#do_install_append_class-native() {
#	install -D -m 0755 ${B}/src/makeguids ${D}${bindir}/makeguids
#}
