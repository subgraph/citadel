DESCRIPTION = "Intelligent Input Bus for Linux/Unix"
LICENSE = "LGPLv2.1"
DEPENDS = "prelink \
           glib-2.0 \
           gsettings-desktop-schemas \
           json-glib \
           gnome-desktop \
           gettext-native \
           intltool-native \
           dconf \
           libnotify \
           gconf-native \
           gtk+ \
          "

LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"

SRC_URI = " \
           https://github.com/ibus/ibus/releases/download/${PV}/${PN}-${PV}.tar.gz \
           file://0001-strip-out-dbus-build-dep.patch \
           file://0002-decorate-automake-for-valaflags.patch \
          "

SRC_URI[md5sum] = "a2be6f200dd9ada2501474a6877a73ef"
SRC_URI[sha256sum] = "4b66c798dab093f0fa738e5c10688d395a463287d13678c208a81051af5d2429"
S = "${WORKDIR}/${PN}-${PV}"

inherit autotools pkgconfig gtk-doc distro_features_check vala gobject-introspection 

FILES_${PN} += "${datadir}"
FILES_${PN} += "${libdir}"

EXTRA_OECONF += " --disable-emoji-dict --disable-unicode-dict --disable-tests "

do_configure_prepend() {
	touch ${S}/ChangeLog
	sed -i "s^@EXTRA_AM_VALAFLAGS@^--vapidir=${RECIPE_SYSROOT_NATIVE}${datadir}/vala-0.38/vapi --vapidir=${B}/bindings/vala --pkg gio-2.0^g" ${S}/tools/Makefile.am
	sed -i "s^@EXTRA_AM_VALAFLAGS@^--vapidir=${RECIPE_SYSROOT_NATIVE}${datadir}/vala-0.38/vapi --vapidir=${B}/bindings/vala --pkg gio-2.0^g" ${S}/engine/Makefile.am
	sed -i "s^@EXTRA_AM_VALAFLAGS@^--vapidir=${RECIPE_SYSROOT_NATIVE}${datadir}/vala-0.38/vapi --vapidir=${B}/bindings/vala --pkg gio-2.0^g" ${S}/ui/gtk3/Makefile.am
}

