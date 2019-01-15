SUMMARY = "GNOME javascript bindings based on the Spidermonkey javascript engine"
HOMEPAGE = "https://wiki.gnome.org/Projects/Gjs"

LICENSE = "MIT & LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=beb29cf17fabe736f0639b09ee6e76fa \
                    file://COPYING.LGPL;md5=3bf50002aefd002f49e7bb854063f7e7"

inherit gnomebase gettext gobject-introspection

export GI_DATADIR="${STAGING_DATADIR}/gobject-introspection-1.0"

DEPENDS = "glib-2.0 gobject-introspection cairo gtk+3 mozjs glib-2.0-native"
EXTRA_OECONF = "--without-dbus-tests"


SRC_URI[archive.md5sum] = "d38565cf77cdef6ef866e7eb77593632"
SRC_URI[archive.sha256sum] = "c1762329eea3632c74653c49e6c7057079b618d4a3e82803d5a9b2bad70a3a57"

# https://gitlab.gnome.org/GNOME/gjs/issues/186
# https://gitlab.gnome.org/GNOME/gjs/issues/187
SRC_URI += "file://overriding_introspected_gobject_interface_properties.patch"
RDEPENDS_${PN} += "libmozjs"

FILES_${PN}-dbg += "${datadir}/gjs-1.0/lsan ${datadir}/gjs-1.0/valgrind"
