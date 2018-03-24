SUMMARY = "Bluetooth integration with GNOME desktop"
HOMEPAGE = "https://wiki.gnome.org/Projects/GnomeBluetooth"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LIB;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI[archive.md5sum] = "75ec82570d0baf18b6cbff86c2712e87"
SRC_URI[archive.sha256sum] = "771472f6df7bf16bdcb2266f4e52b7aa8c5e723509481d734ad22b9ae9fcfe60"


DEPENDS = "glib-2.0 glib-2.0-native gtk+3 libcanberra libnotify libxml2-native gobject-introspection"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext
FILES_${PN} += "${datadir}/icons"


EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

