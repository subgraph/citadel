SUMMARY = "GNOME desktop configuration UI"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=75859989545e37968a99b631ef42722e"

SRC_URI[archive.md5sum] = "79a63122f715d4a8d43dce9f38066e86"
SRC_URI[archive.sha256sum] = "2b1f9bb794cf57838f8340ac8b0e30e6a924c6a3de50c8f2af5f64d6609569bb"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext

DEPENDS = "pulseaudio colord-gtk networkmanager ibus colord gnome-desktop gnome-settings-daemon polkit libcanberra gdk-pixbuf fontconfig gtk+3 glib-2.0 intltool-native upower libpwquality cairo libxml2 libgudev libsoup-2.4 libxi libwacom libx11 libgtop gnome-common-native autoconf-archive-native wayland accountsservice modemmanager network-manager-applet gnome-bluetooth clutter-1.0 clutter-gtk gsettings-desktop-schemas glib-2.0-native libxml2-native"

#
# Extra options have been added to meson_options.txt to make some components of the control
# center optional.  One reason is that these components drag in heavy dependencies, and some
# of these dependencies have not been packaged and tested yet.
# 
# Gnome Online Accounts support
#
#      EXTRA_OEMESON += "-Donline_accounts=true"
#      DEPENDS += "grilo gnome-online-accounts webkitgtk rest"
#
# Printer Panel
#
#      EXTRA_OEMESON += "-Dcups=true"
#      DEPENDS += "cups samba"  (only smbclient needed from samba)
#
# User Accounts Panel
#
#      EXTRA_OEMESON += "-Duser_accounts=true"
#      DEPENDS += "accountsservice krb5"
#
SRC_URI += "file://0001-Make-goa-cups-and-user-accounts-optional.patch" 

FILES_${PN} += "\
    ${datadir}/bash-completion \
    ${datadir}/gettext \
    ${datadir}/dbus-1 \
    ${datadir}/gnome-shell/search-providers \
    ${datadir}/metainfo \
"

EXTRA_OEMESON = "--buildtype=release -Dcheese=false -Ddocumentation=false -Dstaging_dir=${STAGING_DIR_TARGET}"
