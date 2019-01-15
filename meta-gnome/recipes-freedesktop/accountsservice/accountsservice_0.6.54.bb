SUMMARY = "D-Bus service for accessing the list of user accounts and information attached to those accounts."
HOMEPAGE = "https://www.freedesktop.org/wiki/Software/AccountsService"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "https://www.freedesktop.org/software/accountsservice/accountsservice-${PV}.tar.xz"
SRC_URI[md5sum] = "6420f2e619ddcf92230d8f10bad049fe"
SRC_URI[sha256sum] = "26e9062c84797e9604182d0efdb2231cb01c98c3c9b0fea601ca79a2802d21ac"

DEPENDS = "glib-2.0 intltool-native polkit systemd glib-2.0-native dbus" 

inherit meson pkgconfig gettext gobject-introspection

FILES_${PN} += "\
    ${datadir}/dbus-1/interfaces/*.xml \
    ${datadir}/dbus-1/system-services/org.freedesktop.Accounts.service \
    ${datadir}/polkit-1/actions/org.freedesktop.accounts.policy \
    ${systemd_system_unitdir} \
"
EXTRA_OECONF = "--enable-admin-group=wheel"

