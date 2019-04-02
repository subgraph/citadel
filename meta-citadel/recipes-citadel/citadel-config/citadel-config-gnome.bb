DESCRIPTION = ""
HOMEPAGE = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

DEPENDS = "dconf-native"

S = "${WORKDIR}"

SRC_URI = "\
    file://gsettings/90_citadel.gschema.override \
    file://dconf/build.d/initial-realm-dconf.conf \
"

inherit allarch gsettings

# Build the binary dconf database which is installed by default into each new realm
do_compile() {
    mkdir -p ${WORKDIR}/dconf-output
    dconf compile ${WORKDIR}/dconf-output/user ${S}/dconf/build.d
}

do_install() {
    install -d ${D}${datadir}/glib-2.0/schemas
    install -d ${D}${sysconfdir}/skel/.config/dconf

    install -m 644 ${S}/gsettings/90_citadel.gschema.override ${D}${datadir}/glib-2.0/schemas
    install -m 644 ${WORKDIR}/dconf-output/user ${D}${sysconfdir}/skel/.config/dconf
}

FILES_${PN} = "/"
