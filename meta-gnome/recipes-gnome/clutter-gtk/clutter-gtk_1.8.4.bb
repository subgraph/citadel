SUMMARY = "Library for embedding a Clutter canvas (stage) in GTK+"
HOMEPAGE = "https://wiki.gnome.org/Projects/Clutter"

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=7fbc338309ac38fefcd64b04bb903e34" 

SRC_URI = "https://download.gnome.org/sources/clutter-gtk/1.8/clutter-gtk-${PV}.tar.xz"
SRC_URI[md5sum] = "b363ac9878e2337be887b8ee9e1da00e"
SRC_URI[sha256sum] = "521493ec038973c77edcb8bc5eac23eed41645117894aaee7300b2487cb42b06"

DEPENDS = "gtk+3 clutter-1.0"

inherit gettext pkgconfig autotools gobject-introspection

EXTRA_OECONF = ""

