
SUMMARY = "Subgraph OS Citadel image builder"
LICENSE = "MIT"

SYSTEMD_DEFAULT_TARGET = "graphical.target"

require citadel-image-base.bb

ROOTFS_POSTPROCESS_COMMAND += "set_citadel_user_password; symlink_lib64; "

#IMAGE_FSTYPES += "ext2"
IMAGE_FSTYPES = "ext2"

IMAGE_INSTALL += "\
    packagegroup-citadel-desktop \
"

set_blank_user_password() {
    sed -i 's%^citadel:!:%citadel::%' ${IMAGE_ROOTFS}/etc/shadow
}

set_citadel_user_password() {
    # crypt("citadel", "aa")
    sed -i 's%^citadel:!:%citadel:aadg8rGtZzOY6:%' ${IMAGE_ROOTFS}/etc/shadow
}

symlink_lib64() {
    ln -s /usr/lib ${IMAGE_ROOTFS}/lib64
}
