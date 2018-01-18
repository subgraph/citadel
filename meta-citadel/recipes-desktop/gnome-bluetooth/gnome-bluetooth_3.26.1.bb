SUMMARY = "Bluetooth integration with GNOME desktop"
HOMEPAGE = "https://wiki.gnome.org/Projects/GnomeBluetooth"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LIB;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI = "https://download.gnome.org/sources/gnome-bluetooth/3.26/gnome-bluetooth-${PV}.tar.xz"
SRC_URI[md5sum] = "200dff56da5a9ae2649aee38c0d7aac7"
SRC_URI[sha256sum] = "1d2c7b94fc76a833dad0d4d91344e9a5a7b4aad740c5a90944bd25c5be7e784f"

DEPENDS = "glib-2.0 glib-2.0-native gtk+3 libcanberra libnotify libxml2-native gobject-introspection"

FILES_${PN} += "${datadir}/icons"

GIR_SCANNER_NATIVE = "${STAGING_BINDIR_NATIVE}/g-ir-scanner"
GIR_SCRIPT_PATH = "${B}/g-ir-scanner-script"

# https://github.com/ninja-build/ninja/issues/1002
write_gir_script () {
    cat > ${GIR_SCRIPT_PATH} << EOF
#!/bin/sh
export PKG_CONFIG=pkg-config
export PKG_CONFIG_PATH=\"${PKG_CONFIG_PATH}\"
export XDG_DATA_DIRS=\"${STAGING_DATADIR}\"
export CC="x86_64-oe-linux-gcc --sysroot=${STAGING_DIR_HOST}"
export LD="x86_64-oe-linux-gcc --sysroot=${STAGING_DIR_HOST}"
${STAGING_BINDIR}/g-ir-scanner-wrapper \$@
EOF
    chmod +x ${GIR_SCRIPT_PATH}
}

do_configure_append () {
    write_gir_script
    sed --in-place=.old "s;COMMAND = ${GIR_SCANNER_NATIVE};COMMAND = ${GIR_SCRIPT_PATH};" ${B}/build.ninja
}

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

inherit meson gobject-introspection
