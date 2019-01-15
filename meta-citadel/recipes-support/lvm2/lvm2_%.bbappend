PACKAGECONFIG = "odirect udev"

# files/lvm.conf is customized to prevent writing to /etc

#SRC_URI += "file://0001-fix-systemd-generator.patch"

#FILES_${PN} += "\
#    ${systemd_system_unitdir}/lvm2-pvscan@.service \
#    ${systemd_unitdir}/system-generators \
#"

FILES_${PN}-dev += "${libdir}/liblvm2cmd.so*"

#do_install_append() {
#    oe_runmake 'DESTDIR=${D}' install install_systemd_generators
#}
