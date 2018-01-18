SUMMARY = "Text entry and UI navigation application being developed as an alternative to the GNOME On-Screen Keyboard"
HOMEPAGE = "https://wiki.gnome.org/Projects/Caribou"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=e2aa4f66375a24019b0ff5e99cec40ad"

SRC_URI = "https://download.gnome.org/sources/caribou/0.4/caribou-${PV}.tar.xz"
SRC_URI[md5sum] = "16b76cd7453b99e2871e8d4da88bf976"
SRC_URI[sha256sum] = "9c43d9f4bd30f4fea7f780d4e8b14f7589107c52e9cb6bd202bd0d1c2064de55"

DEPENDS = "gtk+3 python3-pygobject intltool-native glib-2.0 clutter-1.0 libgee libxklavier glib-2.0-native xmlto-native python3-pygobject-native"

PYTHON_CARIBOU = "${libdir}/python3.5/site-packages/caribou"
FILES_${PN} += "\
    ${datadir}/dbus-1/services \
    ${datadir}/glib-2.0/schemas \
    ${datadir}/antler \
    ${datadir}/vala/vapi \
    ${libdir}/gtk-3.0/modules/libcaribou-gtk-module.so \
    ${libdir}/gnome-settings-daemon-3.0/gtk-modules \
    ${PYTHON_CARIBOU} \
"

FILES_${PN}-staticdev += "${libdir}/gtk-3.0/modules/libcaribou-gtk-module.a" 

inherit gettext python3native pkgconfig autotools gobject-introspection

export GI_TYPELIB_PATH = "${STAGING_LIBDIR_NATIVE}/girepository-1.0"

EXTRA_OECONF = "--disable-glibtest --enable-gtk2-module=no"

