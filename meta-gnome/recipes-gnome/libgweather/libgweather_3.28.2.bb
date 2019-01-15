SUMMARY = "Location and timezone database and weather lookup library"
HOMEPAGE = "https://wiki.gnome.org/Projects/LibGWeather"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

SRC_URI[archive.md5sum] = "4f8771fedc83bd1c7594c5aa7f21dedb"
SRC_URI[archive.sha256sum] = "081ce81653afc614e12641c97a8dd9577c524528c63772407ae2dbcde12bde75"

DEPENDS = "gtk+3 libxml2 libsoup-2.4 glib-2.0 intltool-native geocode-glib glib-2.0-native"

FILES_${PN} += "${datadir}/glib-2.0/schemas"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext
FILES_${PN} += "${datadir}/icons"

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

