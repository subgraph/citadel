DEPENDS += "paxctl-native"
do_install_append() {
    paxctl -cm ${D}${bindir}/python3.5
}
