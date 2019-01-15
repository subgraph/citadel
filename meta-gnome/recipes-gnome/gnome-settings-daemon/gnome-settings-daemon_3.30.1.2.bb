
SUMMARY = "A set of daemons that manage and provide various parameters to applications"
HOMEPAGE = "https://wiki.gnome.org/Initiatives/Wayland/gnome-settings-daemon"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552 \
                    file://COPYING.LIB;md5=fbc093901857fcd118f065f900982c24"

DEPENDS = "pango gnome-desktop xorgproto libnotify fontconfig libgudev libxext wayland glib-2.0 libxi libx11 libwacom libxtst gsettings-desktop-schemas nss intltool-native gtk+3 polkit upower lcms glib-2.0-native wayland colord geoclue libcanberra geocode-glib libgweather pulseaudio networkmanager"

FILES_${PN} += "\
    ${libdir}/gnome-settings-daemon-3.0 \
"

FILES_${PN}-staticdev += "${libdir}/gnome-settings-daemon-3.0/libgsd.a"

SRC_URI[archive.md5sum] = "bdac5b7329f919f7172f1feb240e48d8"
SRC_URI[archive.sha256sum] = "5cdcf64f6e41fe1816e719850709e8e7a53a11460955f2be77fcee9c80812d1d"

GNOMEBASEBUILDCLASS = "meson"

inherit gnomebase gobject-introspection gettext meson-exe-wrapper

EXTRA_OEMESON += "--buildtype=release -Dcups=false "


# This probably belongs in meson.bbclass
#
# 1) write out a wrapper script that can execute target binaries
#
# 2) add exe_wrapper line to the end of [binaries] section in the 
#    meson.cross file that meson.bbclass generated
#
#setup_wrapper() {
#    if [ ! -e ${B}/wrapper ]; then 
#        cat > ${B}/wrapper << EOF
#!/bin/sh
#${STAGING_LIBDIR}/ld-linux-x86-64.so.2 --library-path ${STAGING_LIBDIR} \$@
#EOF
#        chmod +x ${B}/wrapper
#    fi
#
#    if ! grep -q "^exe_wrapper" ${WORKDIR}/meson.cross; then
#        cat ${WORKDIR}/meson.cross | sed "/pkgconfig/ a\
#exe_wrapper = '${B}/wrapper'" > ${WORKDIR}/meson.cross.tmp
#        mv ${WORKDIR}/meson.cross.tmp ${WORKDIR}/meson.cross
#    fi
#}

#do_configure_prepend() {
#    setup_wrapper
#}
