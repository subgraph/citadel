SUMMARY = "GNOME file manager"
HOMEPAGE = "https://wiki.gnome.org/action/show/Apps/Files"

LICENSE = "GPLv3 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504 \
                    file://libnautilus-extension/LICENSE;md5=321bf41f280cf805086dd5a720b37785"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext

SRC_URI += "file://0001-find-libm-correctly.patch"
SRC_URI[archive.md5sum] = "b929e5c132cdb6e3cbdf216967e7baf0"
SRC_URI[archive.sha256sum] = "b60035352903d792553e5cb2624dcbad927dd6121594a1dfdb7860c5e53bf620"


FILES_${PN} += "\
    ${datadir}/glib-2.0/schemas \
    ${datadir}/gnome-shell/search-providers/nautilus-search-provider.ini \
    ${datadir}/metainfo/org.gnome.Nautilus.appdata.xml \
    ${datadir}/icons/hicolor/ \
    ${datadir}/dbus-1/services \
"

DEPENDS += "gtk+3 glib-2.0 pango gnome-autoar libxml2 gnome-desktop tracker gexiv2"
# This probably belongs in meson.bbclass
#
# 1) write out a wrapper script that can execute target binaries
#
# 2) add exe_wrapper line to the end of [binaries] section in the 
#    meson.cross file that meson.bbclass generated
#
setup_wrapper() {
    if [ ! -e ${B}/wrapper ]; then 
        cat > ${B}/wrapper << EOF
#!/bin/sh
${STAGING_LIBDIR}/ld-linux-x86-64.so.2 --library-path ${STAGING_LIBDIR} \$@
EOF
        chmod +x ${B}/wrapper
    fi

    if ! grep -q "^exe_wrapper" ${WORKDIR}/meson.cross; then
        cat ${WORKDIR}/meson.cross | sed "/pkgconfig/ a\
exe_wrapper = '${B}/wrapper'" > ${WORKDIR}/meson.cross.tmp
        mv ${WORKDIR}/meson.cross.tmp ${WORKDIR}/meson.cross
    fi
}
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
    sed --in-place=.old2 "s;COMMAND = ${GIR_SCANNER_NATIVE};COMMAND = ${GIR_SCRIPT_PATH};" ${B}/build.ninja
}

do_configure_prepend() {
    setup_wrapper
}

EXTRA_OEMESON = "-Denable-selinux=false -Denable-desktop=false -Denable-packagekit=false -Denable-nst-extention=false"
