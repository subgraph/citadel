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

do_configure_prepend() {
    setup_wrapper
}
