LICENSE = "GPLv2" 
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit meson gobject-introspection gettext

def gnome_verdir(v):
    return oe.utils.trim_version(v, 2)

GNOMEBN ?= "${BPN}"
SRC_URI = "${GNOME_MIRROR}/${GNOMEBN}/${@gnome_verdir("${PV}")}/${GNOMEBN}-${PV}.tar.xz;name=archive \
           file://0001-do-no-build-calendar-server.patch \
           file://0001-do-not-use-python-path-from-build-environment.patch \
           file://0001-javascript-invalid-access-fixes.patch \
           file://0001-Disabled-calendar-events-from-user-session.patch \
           "

DEPENDS = "glib-2.0-native systemd libcanberra gtk+3 glib-2.0 libxml2 libcroco mutter libsoup-2.4 gsettings-desktop-schemas gcr atk polkit startup-notification libx11 gnome-bluetooth libsecret networkmanager gjs mozjs52 network-manager-applet pulseaudio libxml2-native paxctl-native ibus xmlto-native sassc-native"

RDEPENDS_${PN} = "xserver-xorg-xwayland cantarell-fonts gnome-theme-adwaita gnome-theme-adwaita-dark gnome-backgrounds gnome-control-center gnome-session adwaita-icon-theme dconf"

FILES_${PN} += "\
    ${datadir}/dbus-1 \
    ${datadir}/glib-2.0/schemas \
    ${datadir}/GConf/gsettings \
    ${datadir}/xdg-desktop-portal \
    ${datadir}/gnome-control-center/keybindings \
"

MUTTER_DIR = "/usr/lib/mutter"
GIR_SCANNER_NATIVE = "${STAGING_BINDIR_NATIVE}/g-ir-scanner"
GIR_SCRIPT_PATH = "${B}/g-ir-scanner-script"

# https://github.com/ninja-build/ninja/issues/1002
write_gir_script () {
    cat > ${GIR_SCRIPT_PATH} << EOF
#!/bin/sh
export PKG_CONFIG=pkg-config
export PKG_CONFIG_PATH=\"${PKG_CONFIG_PATH}\"
export XDG_DATA_DIRS=\"${STAGING_DATADIR}\"
export CC="x86_64-oe-linux-gcc --sysroot=${STAGING_DIR_HOST}"
export LD="x86_64-oe-linux-gcc --sysroot=${STAGING_DIR_HOST}"
${STAGING_BINDIR}/g-ir-scanner-wrapper \$@
EOF
    chmod +x ${GIR_SCRIPT_PATH}
}

do_configure_append () {
    write_gir_script
    sed --in-place=.old1 "s;=${MUTTER_DIR};=${PKG_CONFIG_SYSROOT_DIR}${MUTTER_DIR};" ${B}/build.ninja
    sed --in-place=.old2 "s;COMMAND = ${GIR_SCANNER_NATIVE};COMMAND = ${GIR_SCRIPT_PATH};" ${B}/build.ninja
}

do_install_append() {
    paxctl -cm ${D}${bindir}/gnome-shell
}

EXTRA_OEMESON = "--buildtype=release -Dbrowser_plugin=false -Dman=false -Dsystemd=true -Dnetworkmanager=true"
