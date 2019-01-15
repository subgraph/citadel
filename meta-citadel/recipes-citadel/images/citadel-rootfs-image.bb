
SUMMARY = "Subgraph OS Citadel image builder"
LICENSE = "MIT"

SYSTEMD_DEFAULT_TARGET = "graphical.target"

ROOTFS_POSTPROCESS_COMMAND += "set_citadel_user_password; symlink_lib64; setup_var; append_os_release;"

IMAGE_INSTALL += "\
    packagegroup-citadel-base \
    packagegroup-citadel \
"

CITADEL_IMAGE_VERSION = "${CITADEL_IMAGE_VERSION_rootfs}"
CITADEL_IMAGE_TYPE = "rootfs"

require citadel-image.inc
inherit citadel-image

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

    cp ${IMAGE_ROOTFS}/etc/skel/.??* ${IMAGE_ROOTFS}/home/citadel
    cp ${IMAGE_ROOTFS}/etc/skel/.??* ${IMAGE_ROOTFS}/home/root
    rm -rf ${IMAGE_ROOTFS}/etc/skel
    mv ${IMAGE_ROOTFS}/home/citadel ${IMAGE_ROOTFS}/usr/share/factory/home
    mv ${IMAGE_ROOTFS}/home/root ${IMAGE_ROOTFS}/usr/share/factory/home

    rm ${IMAGE_ROOTFS}/usr/share/gnome-shell/gnome-shell-theme.gresource
    ln -sf /opt/share/themes/Adapta-Eta/gnome-shell/gnome-shell-theme.gresource ${IMAGE_ROOTFS}/usr/share/gnome-shell/gnome-shell-theme.gresource

    ln -sf /opt/share/themes/Adapta ${IMAGE_ROOTFS}/usr/share/themes/Adapta
    ln -sf /opt/share/themes/Adapta-Eta ${IMAGE_ROOTFS}/usr/share/themes/Adapta-Eta
    ln -sf /opt/share/themes/Adapta-Nokto ${IMAGE_ROOTFS}/usr/share/themes/Adapta-Nokto
    ln -sf /opt/share/themes/Adapta-Nokto-Eta ${IMAGE_ROOTFS}/usr/share/themes/Adapta-Nokto-Eta
    ln -sf /opt/share/icons/Paper ${IMAGE_ROOTFS}/usr/share/icons/Paper
    ln -sf /opt/share/icons/Adwaita ${IMAGE_ROOTFS}/usr/share/icons/Adwaita
    ln -sf /opt/share/backgrounds/gnome ${IMAGE_ROOTFS}/usr/share/backgrounds/gnome
    ln -sf /opt/share/gnome-background-properties ${IMAGE_ROOTFS}/usr/share/gnome-background-properties

    # do_rootfs() will fail otherwise
    ln -sf ../usr/share/factory/var/lib ${IMAGE_ROOTFS}/var/lib
}

append_os_release() {
    echo "CITADEL_CHANNEL=\"${CITADEL_IMAGE_CHANNEL}\"" >> ${IMAGE_ROOTFS}/etc/os-release
    echo "CITADEL_ROOTFS_VERSION=\"${CITADEL_IMAGE_VERSION_rootfs}\"" >> ${IMAGE_ROOTFS}/etc/os-release
}

do_rm_var_link() {
    rm ${IMAGE_ROOTFS}/var/lib
}

addtask rm_var_link after do_rootfs before do_image_qa

symlink_lib64() {
    ln -s /usr/lib ${IMAGE_ROOTFS}/lib64
}
