LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI[archive.md5sum] = "d74b9bf421b2b82ebfe11cccc055a760"
SRC_URI[archive.sha256sum] = "df24dcc0b866fc6bffbfc82881a84bd7cc9c641e4124d2545c368c0b10e12363"

SRC_URI_append = "\
    file://0001-remove-check-for-zenity.patch \
    file://startup-notification.patch \
    file://get_client_pid.patch \
"

DEPENDS = "libxrandr libsm libx11 libxi glib-2.0 wayland-protocols libwacom mesa gtk+3 pango cairo gsettings-desktop-schemas xcomposite upower gnome-desktop libxkbfile json-glib wayland-native xinerama zenity libinput libcanberra"
RDEPENDS_${PN} = "zenity"

inherit gettext pkgconfig autotools gobject-introspection gnome

FILES_${PN} += "${datadir}/gnome-control-center"

do_compile_prepend() {
    export GIR_EXTRA_LIBS_PATH="${B}/cogl/cogl/.libs:${B}/cogl/cogl-pango/.libs:${B}/cogl/cogl-path/.libs:${B}/clutter/clutter/.libs"
}

EXTRA_OECONF = "--with-gudev --with-libwacom"

