LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://COPYING.LIB;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "https://www.freedesktop.org/software/ModemManager/ModemManager-${PV}.tar.xz"
SRC_URI[md5sum] = "d21c280220ee647e62eeb4e1df642fe3"
SRC_URI[sha256sum] = "eefb7615c2c7ebc994abfc2782bfa9e798643a633362b40db96f7f61706a6283"

S = "${WORKDIR}/ModemManager-${PV}"

DEPENDS = "libgudev intltool-native glib-2.0 systemd polkit glib-2.0-native"

FILES_${PN} += "\
    ${libdir}/ModemManager \
    ${libdir}/rules.d \
    ${datadir}/bash-completion \
    ${datadir}/polkit-1/actions \
    ${datadir}/icons \
    ${datadir}/dbus-1 \
"
SYSTEMD_SERVICE_${PN} = "ModemManager.service"

inherit pkgconfig gettext autotools systemd

# --with-udev-base-dir set for usr merge
EXTRA_OECONF = "--disable-introspection --disable-vala --without-mbim --without-qmi --with-udev-base-dir=/usr/lib --enable-more-warnings=no"

