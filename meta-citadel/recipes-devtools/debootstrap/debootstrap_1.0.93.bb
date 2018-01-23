SUMMARY = "Install a Debian system into a subdirectory"
HOMEPAGE = "https://wiki.debian.org/Debootstrap"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://debian/copyright;md5=1e68ced6e1689d4cd9dac75ff5225608"

inherit pkgconfig

SRC_URI  = "\
    http://http.debian.net/debian/pool/main/d/debootstrap/debootstrap_${PV}.tar.gz \
    file://devices.tar.gz;unpack=0 \
    file://pkgdetails.c \
"

SRC_URI[md5sum] = "db30bdbf17d63d35a2cf28ba343db734"
SRC_URI[sha256sum] = "cdad4d2be155bd933acbe4f3479e1765e5f4447fb50564e30e33f7b3b84bd7db"


S = "${WORKDIR}/${BPN}"

# All Makefile does is creation of devices.tar.gz, which fails in OE build, we use
# static devices.tar.gz as work around
# | NOTE: make -j 8 -e MAKEFLAGS=
# | rm -rf dev
# | mkdir -p dev
# | chown 0:0 dev
# | chown: changing ownership of `dev': Operation not permitted
# | make: *** [devices.tar.gz] Error 1
# | WARNING: exit code 1 from a shell command.
do_compile_prepend() {
    cp ${WORKDIR}/devices.tar.gz ${B}
    ${CC} ${CFLAGS} ${LDFLAGS} ${WORKDIR}/pkgdetails.c -o ${WORKDIR}/pkgdetails
}

do_install() {
    oe_runmake 'DESTDIR=${D}' install
    chown -R root:root ${D}${datadir}/debootstrap
    install -d ${D}${libdir}/debootstrap
    install -m 755 ${WORKDIR}/pkgdetails ${D}${libdir}/debootstrap
}
