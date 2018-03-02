SUMMARY = "Framework for discovery and browsing of network accessible media"
HOMEPAGE = "https://wiki.gnome.org/Projects/Grilo"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"

SRC_URI[archive.md5sum] = "a15a92a903aeb7579e1b0f6e8b4b0fb1"
SRC_URI[archive.sha256sum] = "7c6964053b42574c2f14715d2392a02ea5cbace955eb73e067c77aa3e43b066e"

DEPENDS = "glib-2.0 gtk+3 libsoup-2.4 intltool-native libxml2 gnome-common-native"

inherit gettext gobject-introspection gnome

EXTRA_OECONF = "--disable-grl-net --disable-grl-pls --disable-test-ui --disable-vala"

