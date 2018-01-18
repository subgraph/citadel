LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI[archive.md5sum] = "137724b82820db992d9df103959e20fc"
SRC_URI[archive.sha256sum] = "16faf617aae9be06dc5f9e104f4cd20dfdd4d6ec0bc10053752262e9f79a04c2"
SRC_URI_append = " file://0001-remove-check-for-zenity.patch" 

DEPENDS = "libxrandr libsm libx11 libxi glib-2.0 wayland-protocols libwacom mesa gtk+3 pango cairo gsettings-desktop-schemas xcomposite upower gnome-desktop libxkbfile json-glib wayland-native xinerama zenity libinput libcanberra"
RDEPENDS_${PN} = "zenity"

inherit gettext pkgconfig autotools gobject-introspection gnome

FILES_${PN} += "${datadir}/gnome-control-center"

do_compile_prepend() {
    export GIR_EXTRA_LIBS_PATH="${B}/cogl/cogl/.libs:${B}/cogl/cogl-pango/.libs:${B}/cogl/cogl-path/.libs:${B}/clutter/clutter/.libs"
}

EXTRA_OECONF = "--with-gudev --with-libwacom"

