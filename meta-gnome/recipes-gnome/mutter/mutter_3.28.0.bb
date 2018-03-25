LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI[archive.md5sum] = "7a3baf2fbb02f9cc341bf0424a31d0d2"
SRC_URI[archive.sha256sum] = "58fffc8025f21fb6da27bd2189b6db4d20c54f950b1a46aa7f7cbf0a82d386b0"
SRC_URI_append = "\
    file://0001-remove-check-for-zenity.patch \
    file://0002-avoid-unnecessary-relayouts-in-cluttertext.patch \
    file://startup-notification.patch \
"

DEPENDS = "libxrandr libsm libx11 libxi glib-2.0 wayland-protocols libwacom mesa gtk+3 pango cairo gsettings-desktop-schemas xcomposite upower gnome-desktop libxkbfile json-glib wayland-native xinerama zenity libinput libcanberra"
RDEPENDS_${PN} = "zenity"

inherit gettext pkgconfig autotools gobject-introspection gnome

FILES_${PN} += "${datadir}/gnome-control-center"

do_compile_prepend() {
    export GIR_EXTRA_LIBS_PATH="${B}/cogl/cogl/.libs:${B}/cogl/cogl-pango/.libs:${B}/cogl/cogl-path/.libs:${B}/clutter/clutter/.libs"
}

EXTRA_OECONF = "--with-gudev --with-libwacom"

