SUMMARY = "Assistive Technology Service Provider Interface (dbus core)"
HOMEPAGE = "https://wiki.linuxfoundation.org/accessibility/d-bus"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=e9f288ba982d60518f375b5898283886"

SRC_URI[archive.md5sum] = "9c42f79636ed1c0e908b7483d789b32e"
SRC_URI[archive.sha256sum] = "42a2487ab11ce43c288e73b2668ef8b1ab40a0e2b4f94e80fca04ad27b6f1c87"


DEPENDS = "dbus glib-2.0 virtual/libx11 libxi libxtst"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext systemd distro_features_check upstream-version-is-even
# depends on virtual/libx11
REQUIRED_DISTRO_FEATURES = "x11"

#EXTRA_OEMESON = "--buildtype=release -Denable-introspection=yes -Ddbus-daemon=${bindir}"
EXTRA_OEMESON = "--buildtype=release -Denable-introspection=yes -Ddbus_daemon=${bindir}/dbus-daemon" 

FILES_${PN} += "${datadir}/dbus-1/services/*.service \
                ${datadir}/dbus-1/accessibility-services/*.service \
                ${datadir}/defaults/at-spi2 \
                ${systemd_user_unitdir}/at-spi-dbus-bus.service \
                "
