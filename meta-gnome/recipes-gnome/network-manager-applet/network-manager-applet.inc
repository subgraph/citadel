SUMMARY = "Applet for managing network connections"
HOMEPAGE = "https://wiki.gnome.org/Projects/NetworkManager"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"

DEPENDS = "gcr iso-codes networkmanager libgudev gtk+3 intltool-native modemmanager glib-2.0 libnotify libsecret glib-2.0-native"

inherit gettext gobject-introspection gnome

do_compile_prepend() {
    export GIR_EXTRA_LIBS_PATH="${B}/src/libnma/.libs:${B}/src/utils/.libs:${B}/src/wireless-security/.libs:${B}/src/libnm-gtk/.libs"
}

EXTRA_OECONF = "--without-selinux --without-team --enable-introspection --with-libnm-gtk"

