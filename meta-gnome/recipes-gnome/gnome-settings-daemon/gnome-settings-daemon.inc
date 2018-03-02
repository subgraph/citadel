SUMMARY = "A set of daemons that manage and provide various parameters to applications"
HOMEPAGE = "https://wiki.gnome.org/Initiatives/Wayland/gnome-settings-daemon"
LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552 \
                    file://COPYING.LIB;md5=fbc093901857fcd118f065f900982c24"

DEPENDS = "pango gnome-desktop kbproto libnotify fontconfig libgudev libxext wayland glib-2.0 libxi libx11 libwacom libxtst gsettings-desktop-schemas nss intltool-native gtk+3 polkit upower lcms glib-2.0-native wayland colord geoclue libcanberra geocode-glib libgweather pulseaudio networkmanager"

FILES_${PN} += "\
    ${libdir}/gnome-settings-daemon-3.0 \
"

FILES_${PN}-staticdev += "${libdir}/gnome-settings-daemon-3.0/libgsd.a"

inherit gettext gnome

EXTRA_OECONF = "--disable-cups"

