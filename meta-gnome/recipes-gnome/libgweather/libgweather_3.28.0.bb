SUMMARY = "Location and timezone database and weather lookup library"
HOMEPAGE = "https://wiki.gnome.org/Projects/LibGWeather"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI[archive.md5sum] = "767b4af6ce55bda74cb2f53aaefcfc6f"
SRC_URI[archive.sha256sum] = "594be78dcc0b4c48bf79cd42ea6768160b661bc2a74d9d35ecc742575416e18f"

DEPENDS = "gtk+3 libxml2 libsoup-2.4 glib-2.0 intltool-native geocode-glib glib-2.0-native"

FILES_${PN} += "${datadir}/glib-2.0/schemas"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext
FILES_${PN} += "${datadir}/icons"

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

