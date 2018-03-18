FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://gnome-terminal-citadel.service \
    file://org.gnome.TerminalCitadel.desktop \
    file://org.gnome.TerminalCitadel.service \
    file://50_gnome-terminal.gschema.override \
"

DEPENDS += "paxctl-native"

do_install_append() {
    install -m 644 ${WORKDIR}/50_gnome-terminal.gschema.override ${D}${datadir}/glib-2.0/schemas
    install -m 644 ${WORKDIR}/org.gnome.TerminalCitadel.desktop ${D}${datadir}/applications
    install -m 644 ${WORKDIR}/org.gnome.TerminalCitadel.service ${D}${datadir}/dbus-1/services
    install -m 644 ${WORKDIR}/gnome-terminal-citadel.service ${D}${systemd_user_unitdir}
    rm ${D}${datadir}/applications/org.gnome.Terminal.desktop
    paxctl -cm ${D}${libexecdir}/gnome-terminal-server
}


