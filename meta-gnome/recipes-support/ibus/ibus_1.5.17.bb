SUMMARY = "Intelligent input bus for Linux/Unix"
HOMEPAGE = "https://github.com/ibus/ibus/wiki"

LICENSE = "Unicode & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING.unicode;md5=53c2b911a997d7831df75e1c37f3c4b4 \
                    file://COPYING;md5=fbc093901857fcd118f065f900982c24"

SRC_URI = "https://github.com/ibus/ibus/releases/download/${PV}/ibus-${PV}.tar.gz"
SRC_URI[md5sum] = "8bb26453d0d1fa58e56c22668aaa8786"
SRC_URI[sha256sum] = "0347a8055977ca458e8add750af5f9b76e1a524844cc3b0e2fad70ce153dd219"

S = "${WORKDIR}/${BPN}-${PV}"

FILES_${PN} += "\
    ${datadir}/icons/hicolor \
    ${datadir}/GConf/gsettings \
    ${datadir}/dbus-1/services \
    ${datadir}/bash-completion/completions \
    ${datadir}/glib-2.0/schemas \
    ${libdir}/gtk-3.0/3.0.0/immodules/im-ibus.so \
"

DEPENDS = "libx11 wayland intltool-native libnotify iso-codes dbus dconf libxkbcommon glib-2.0 gtk+3 gobject-introspection gconf glib-2.0-native"

inherit gettext pythonnative pkgconfig gconf autotools gobject-introspection vala

EXTRA_OECONF = "--disable-gtk2 --disable-tests --enable-wayland --disable-emoji-dict --disable-python-library"

