SUMMARY = "Convenience library for geocoding (finding longitude and latitude from a street address)"
HOMEPAGE = "https://developer.gnome.org/geocode-glib/stable/"

LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=55ca817ccb7d5b5b66355690e9abc605" 

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext gobject-introspection

SRC_URI[archive.md5sum] = "98c0a7d175014d5865be7d3f774ef14c"
SRC_URI[archive.sha256sum] = "ea4086b127050250c158beff28dbcdf81a797b3938bb79bbaaecc75e746fbeee"   

FILES_${PN} += "${datadir}/icons/gnome"

DEPENDS = "json-glib libsoup-2.4 glib-2.0 glib-2.0-native"
EXTRA_OEMESON = "-Denable-installed-tests=false -Denable-gtk-doc=false --buildtype=release"

