SUMMARY = "GNOME desktop configuration UI"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=75859989545e37968a99b631ef42722e"

inherit gettext gnome

SRC_URI += "\
           file://0001-Remove-grilo-miner-from-background-panel.patch \
           file://0002-remove-unwanted-panels.patch \
           file://0003-disable-running-tests.patch \
           file://0004-remove-unwanted-panel-dependencies.patch \
           file://0005-fix-gdesktop-prefix.patch \
           "

DEPENDS = "pulseaudio colord-gtk networkmanager ibus colord gnome-desktop gnome-settings-daemon polkit libcanberra gdk-pixbuf fontconfig gtk+3 glib-2.0 intltool-native upower libpwquality cairo libxml2 libgudev libsoup-2.4 libxi libwacom libx11 libgtop gnome-common-native autoconf-archive-native wayland accountsservice modemmanager nm-applet gnome-bluetooth clutter-1.0 clutter-gtk gsettings-desktop-schemas glib-2.0-native libxml2-native"

FILES_${PN} += "\
    ${datadir}/bash-completion \
    ${datadir}/gettext \
    ${datadir}/dbus-1 \
    ${datadir}/gnome-shell/search-providers \
"

EXTRA_OECONF = "--disable-documentation --without-cheese"

