SUMMARY = "Bluetooth integration with GNOME desktop"
HOMEPAGE = "https://wiki.gnome.org/Projects/GnomeBluetooth"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LIB;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI[archive.md5sum] = "eff705fadd5e0bb44a087d10fcbe34af"
SRC_URI[archive.sha256sum] = "1f4961ac33b5f9ce5e6e01ea2c08ad29cc98ed2b8878e38da867235b28a50732"

DEPENDS = "glib-2.0 glib-2.0-native gtk+3 libcanberra libnotify libxml2-native gobject-introspection"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext
FILES_${PN} += "${datadir}/icons"

EXTRA_OEMESON = "--buildtype=release -Denable-introspection=true"

