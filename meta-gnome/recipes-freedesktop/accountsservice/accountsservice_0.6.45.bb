SUMMARY = "D-Bus service for accessing the list of user accounts and information attached to those accounts."
HOMEPAGE = "https://www.freedesktop.org/wiki/Software/AccountsService"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "https://www.freedesktop.org/software/accountsservice/accountsservice-${PV}.tar.xz"
SRC_URI[md5sum] = "b4c0a74bb5f8680dda0b7be27b1c02d9"
SRC_URI[sha256sum] = "fb0fc293aa75d59f5ef5db719d37a21831c4dd74a97526ee7e51ce936311ef26"

DEPENDS = "glib-2.0 intltool-native polkit systemd glib-2.0-native" 

inherit pkgconfig gettext autotools gobject-introspection

FILES_${PN} += "\
    ${datadir}/dbus-1/interfaces/*.xml \
    ${datadir}/dbus-1/system-services/org.freedesktop.Accounts.service \
    ${datadir}/polkit-1/actions/org.freedesktop.accounts.policy \
    ${systemd_system_unitdir} \
"
EXTRA_OECONF = "--enable-admin-group=wheel"

