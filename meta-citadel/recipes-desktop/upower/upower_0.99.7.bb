SUMMARY = "Linux power management daemon"
HOMEPAGE = "http://upower.freedesktop.org"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=0de8fbf1d97a140d1d93b9f14dcfbf08 \
                    file://doc/html/license.html;md5=dd77cfbf0965ebe2f167827e6ae5f63f"

SRC_URI = "https://upower.freedesktop.org/releases/upower-${PV}.tar.xz"
SRC_URI[md5sum] = "236bb439d9ff1151450b3d8582399532"
SRC_URI[sha256sum] = "24bcc2f6ab25a2533bac70b587bcb019e591293076920f5b5e04bdedc140a401"

DEPENDS = "libusb1 glib-2.0 intltool-native libgudev"

inherit gettext pkgconfig autotools gobject-introspection

FILES_${PN} += "\
    ${datadir}/dbus-1 \
    ${systemd_system_unitdir} \
"

EXTRA_OECONF = "--enable-man-pages=no --enable-gtk-doc=no"

