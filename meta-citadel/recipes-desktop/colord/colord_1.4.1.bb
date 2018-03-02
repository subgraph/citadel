SUMMARY = "System service to manage, install and generate color profiles to color manage input and output devices"
HOMEPAGE ="https://www.freedesktop.org/software/colord/"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI = "https://www.freedesktop.org/software/colord/releases/colord-${PV}.tar.xz \
           file://0001-remove-gobject-introspection.patch \
           "
SRC_URI[md5sum] = "f457be5b7c44827e6c747ec80a6dc69a"
SRC_URI[sha256sum] = "2b068fc8298265a7a3b68e7516c7a263394cff57579af0d1c0fb6b7429230555"

DEPENDS = "glib-2.0-native lcms libgusb libgudev polkit"
EXTRA_OEMESON = "-Denable-argyllcms-sensor=false -Denable-man=false -Denable-docs=false -Dwith-daemon-user=colord -Denable-bash-completion=false -Denable-systemd=true -Denable-tests=false -Denable-print-profiles=false --buildtype=release"

PACKAGES += "${PN}-plugins"

FILES_${PN} += "\
    ${datadir}/glib-2.0/schemas \
    ${datadir}/polkit-1 \
    ${datadir}/dbus-1 \
    ${datadir}/color/icc/colord \
    ${libdir}/tmpfiles.d \
    ${systemd_user_unitdir}/colord-session.service \
"
SYSTEMD_SERVICE_${PN} = "colord.service"

FILES_${PN}-plugins = "\
    ${libdir}/colord-plugins \
    ${libdir}/colord-sensors \
"

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

do_configure_prepend() {
    setup_wrapper
}

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "--system --home /var/lib/colord --no-create-home --shell /bin/false --user-group colord"
inherit meson systemd useradd gettext

