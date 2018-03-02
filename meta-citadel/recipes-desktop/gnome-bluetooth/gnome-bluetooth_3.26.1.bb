SUMMARY = "Bluetooth integration with GNOME desktop"
HOMEPAGE = "https://wiki.gnome.org/Projects/GnomeBluetooth"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LIB;md5=a6f89e2100d9b6cdffcea4f398e37343"

#SRC_URI = "https://download.gnome.org/sources/gnome-bluetooth/3.26/gnome-bluetooth-${PV}.tar.xz"
SRC_URI[archive.md5sum] = "200dff56da5a9ae2649aee38c0d7aac7"
SRC_URI[archive.sha256sum] = "1d2c7b94fc76a833dad0d4d91344e9a5a7b4aad740c5a90944bd25c5be7e784f"

DEPENDS = "glib-2.0 glib-2.0-native gtk+3 libcanberra libnotify libxml2-native gobject-introspection"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext
FILES_${PN} += "${datadir}/icons"


EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

