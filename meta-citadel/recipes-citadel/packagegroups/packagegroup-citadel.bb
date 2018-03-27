
inherit packagegroup

RDEPENDS_${PN} = "\
    packagegroup-citadel-base \
    packagegroup-desktop \
    packagegroup-gnome \
    packagegroup-sway \
    packagegroup-theme \
    launch-default-realm \
    citadel-rootfs \
    citadel-realms \
"
