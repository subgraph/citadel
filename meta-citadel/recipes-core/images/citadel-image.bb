
SUMMARY = "Subgraph OS Citadel image builder"
LICENSE = "MIT"

SYSTEMD_DEFAULT_TARGET = "graphical.target"

require citadel-image-base.bb

ROOTFS_POSTPROCESS_COMMAND += "set_citadel_user_password; symlink_lib64; setup_var; "

#IMAGE_FSTYPES += "ext2"
IMAGE_FSTYPES = "ext2"

IMAGE_INSTALL += "\
    packagegroup-citadel \
"

set_blank_user_password() {
    sed -i 's%^citadel:!:%citadel::%' ${IMAGE_ROOTFS}/etc/shadow
}

set_citadel_user_password() {
    # crypt("citadel", "aa")
    sed -i 's%^citadel:!:%citadel:aadg8rGtZzOY6:%' ${IMAGE_ROOTFS}/etc/shadow
}

setup_var() {
    install -m 0755 -d ${IMAGE_ROOTFS}/usr/share/factory/var
    install -m 0755 -d ${IMAGE_ROOTFS}/usr/share/factory/home

    install -m 0755 -d ${IMAGE_ROOTFS}/home/citadel/.local/share/applications
    chown -R 1000:1000 ${IMAGE_ROOTFS}/home/citadel/.local

    rmdir ${IMAGE_ROOTFS}/var/log/journal

    mv ${IMAGE_ROOTFS}/var/lib ${IMAGE_ROOTFS}/usr/share/factory/var
    mv ${IMAGE_ROOTFS}/var/log ${IMAGE_ROOTFS}/usr/share/factory/var
    mv ${IMAGE_ROOTFS}/var/cache  ${IMAGE_ROOTFS}/usr/share/factory/var
    mv ${IMAGE_ROOTFS}/var/spool ${IMAGE_ROOTFS}/usr/share/factory/var
    mv ${IMAGE_ROOTFS}/var/run ${IMAGE_ROOTFS}/usr/share/factory/var
    mv ${IMAGE_ROOTFS}/var/lock ${IMAGE_ROOTFS}/usr/share/factory/var

    mv ${IMAGE_ROOTFS}/home/citadel ${IMAGE_ROOTFS}/usr/share/factory/home
    cp ${IMAGE_ROOTFS}/etc/skel/.bashrc ${IMAGE_ROOTFS}/home/root
    cp ${IMAGE_ROOTFS}/etc/skel/.profile ${IMAGE_ROOTFS}/home/root
    cp ${IMAGE_ROOTFS}/etc/skel/.vimrc ${IMAGE_ROOTFS}/home/root
    mv ${IMAGE_ROOTFS}/home/root ${IMAGE_ROOTFS}/usr/share/factory/home

    rm ${IMAGE_ROOTFS}/usr/share/gnome-shell/gnome-shell-theme.gresource
    ln -sf ../themes/Adapta-Nokto/gnome-shell/gnome-shell-theme.gresource ${IMAGE_ROOTFS}/usr/share/gnome-shell/gnome-shell-theme.gresource

    # do_rootfs() will fail otherwise
    ln -sf ../usr/share/factory/var/lib ${IMAGE_ROOTFS}/var/lib
}

do_rm_var_link() {
    rm ${IMAGE_ROOTFS}/var/lib
}

addtask rm_var_link after do_rootfs before do_image_qa

symlink_lib64() {
    ln -s /usr/lib ${IMAGE_ROOTFS}/lib64
}
