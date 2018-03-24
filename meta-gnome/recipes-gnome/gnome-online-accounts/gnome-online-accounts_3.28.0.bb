LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=34c88b124db5fb2762c1676be7dadd36" 

SRC_URI[archive.md5sum] = "25827cac1609cf1b5a089d9615d47d86"
SRC_URI[archive.sha256sum] = "87bc4ef307604f1ce4f09f6e5c9996ef8d37ca5e0a3bf76f6b27d71844adb40c"

DEPENDS = "libxml2 libsoup-2.4 libsecret gcr json-glib gtk+3 glib-2.0 glib-2.0-native xmlto-native webkitgtk rest"

inherit gnome gettext gobject-introspection vala

EXTRA_OECONF = "--disable-Werror"

