SUMMARY = "Convenience library for geocoding (finding longitude and latitude from a street address)"
HOMEPAGE = "https://developer.gnome.org/geocode-glib/stable/"

LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING.LIB;md5=55ca817ccb7d5b5b66355690e9abc605" 

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/geocode-glib/3.25/geocode-glib-${PV}.tar.xz"
SRC_URI[md5sum] = "7315e7ff162dfd01165ba8a284e3eda6"
SRC_URI[sha256sum] = "f10169262c313dfaa21acf00687c01e0aaf52983524648e8b9e8e42c052dd778"

FILES_${PN} += "${datadir}/icons/gnome"

DEPENDS = "json-glib libsoup-2.4 glib-2.0 glib-2.0-native"
EXTRA_OEMESON = "-Denable-installed-tests=false -Denable-introspection=false -Denable-gtk-doc=false --buildtype=release"

inherit meson
