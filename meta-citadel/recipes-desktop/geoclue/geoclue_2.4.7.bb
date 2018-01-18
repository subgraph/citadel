SUMMARY = "Provides D-Bus service makes location information available to location-aware applications"
HOMEPAGE = "https://www.freedesktop.org/wiki/Software/GeoClue/"

LICENSE = "LGPLv2.1 & GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=8114b83a0435d8136b47bd70111ce5cd \
                    file://COPYING.LIB;md5=4b54a1fd55a448865a0b32d41598759d"

SRC_URI = "https://www.freedesktop.org/software/geoclue/releases/2.4/geoclue-${PV}.tar.xz"
SRC_URI[md5sum] = "e50086e742740413669ab72d8572db05"
SRC_URI[sha256sum] = "d17b96bb5799a84723385ea5704235565e9c3dedd2b7afac475a06e550ae0ea6"

DEPENDS = "json-glib glib-2.0 libnotify intltool-native libsoup-2.4"
FILES_${PN} += "/usr/lib/systemd /usr/share/dbus-1"

inherit gettext pkgconfig autotools gobject-introspection

EXTRA_OECONF = "--disable-gtk-doc --disable-3g-source --disable-modem-gps-source --disable-cdma-source --disable-nmea-source"

