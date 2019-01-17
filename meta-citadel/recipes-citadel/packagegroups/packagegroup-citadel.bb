
inherit packagegroup

RDEPENDS_${PN} = "\
    packagegroup-citadel-base \
    packagegroup-desktop \
    packagegroup-gnome \
    packagegroup-sway \
    packagegroup-theme \
    citadel-tools-mount \
    citadel-tools-install \
    citadel-tools-realms \
    citadel-tools-image \
"
