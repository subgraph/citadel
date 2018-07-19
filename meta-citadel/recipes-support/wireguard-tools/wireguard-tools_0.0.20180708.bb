require wireguard-tools.inc

inherit bash-completion systemd pkgconfig

DEPENDS = "libmnl"

do_compile_prepend () {
    cd ${S}/tools
}

do_unpack () {
    tar -xvf ${DL_DIR}/WireGuard-0.0.20180708.tar.xz -C ${WORKDIR}/
    # Remove symlink pointing to non-existent file as this causes the
    # reproducible_build .bbclass to fail
    rm ${WORKDIR}/WireGuard-0.0.20180708/src/tools/wg-quick/wg
}

do_install () {
    cd ${S}/tools
    oe_runmake DESTDIR="${D}" PREFIX="${prefix}" SYSCONFDIR="${sysconfdir}" \
        SYSTEMDUNITDIR="${systemd_unitdir}" \
        WITH_SYSTEMDUNITS=${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'yes', '', d)} \
        WITH_BASHCOMPLETION=yes \
        WITH_WGQUICK=yes \
        install
}

FILES_${PN} = " \
    ${sysconfdir} \
    ${systemd_unitdir} \
    ${bindir} \
"

RDEPENDS_${PN} = "bash"
