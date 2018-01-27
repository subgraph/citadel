SUMMARY = "GNOME javascript bindings based on the Spidermonkey javascript engine"
HOMEPAGE = "https://wiki.gnome.org/Projects/Gjs"

LICENSE = "MIT & LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=e7436dd55d7c05a5fa18f8cab10b3a6b \
                    file://COPYING.LGPL;md5=3bf50002aefd002f49e7bb854063f7e7"

inherit gnomebase gettext gobject-introspection

export GI_DATADIR="${STAGING_DATADIR}/gobject-introspection-1.0"

DEPENDS = "glib-2.0 gobject-introspection cairo gtk+3 mozjs52 glib-2.0-native"
EXTRA_OECONF = "--without-dbus-tests"

