SUMMARY = "GNOME management of secrets, passwords, keys, and certificates"
HOMEPAGE = "https://wiki.gnome.org/Projects/GnomeKeyring"

LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

DEPENDS = "gcr intltool-native glib-2.0-native libpam"
RDEPENDS_${PN} = "gcr libpam"
FILES_${PN} += "\
    ${libdir}/pkcs11 \
    ${libdir}/security \
    ${datadir}/p11-kit \
    ${datadir}/dbus-1 \
"

inherit gettext gnome

EXTRA_OECONF = "--disable-doc --disable-ssh-agent"

