
DESCRIPTION = "Citadel resources"
LICENSE = "MIT"

PACKAGE_INSTALL = "\
    linux-firmware \
    adapta-gtk-theme \
    paper-icon-theme \
    gnome-backgrounds \
    base16-vim \
    adwaita-icon-theme \
    adwaita-icon-theme-cursors \
    adwaita-icon-theme-hires \
    adwaita-icon-theme-symbolic \
    adwaita-icon-theme-symbolic-hires \
"
CITADEL_IMAGE_VERSION = "${CITADEL_IMAGE_VERSION_extra}"
CITADEL_IMAGE_TYPE = "extra"

require citadel-image.inc
inherit citadel-image

ROOTFS_POSTPROCESS_COMMAND += "write_manifest_file; "

write_manifest_file() {
    cat > ${IMAGE_ROOTFS}/manifest << EOF
/usr/lib/firmware
/usr/share:/opt/share
EOF
}
