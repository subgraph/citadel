DESCRIPTION = ""
HOMEPAGE = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = "mtools-native syslinux-native dosfstools-native coreutils-native parted-native"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy
require citadel-image.inc

KERNEL_CMDLINE = "root=/dev/mapper/rootfs citadel.verbose fstab=no luks=no splash"

do_rootfs() {
    install_efi_files
    install_syslinux_files
    install_image_files
}

do_rootfs[dirs] = "${TOPDIR}"
do_rootfs[cleandirs] = "${S} ${IMAGE_ROOTFS}"
do_rootfs[umask] = "022"
do_rootfs[depends] += "\
    citadel-rootfs-image:do_image_complete \
    citadel-extra-image:do_image_complete \
    citadel-kernel-image:do_image_complete \
    citadel-kernel:do_deploy \
    systemd-boot:do_deploy \
"
addtask rootfs after do_configure before do_build

install_efi_files() {
    install -d ${IMAGE_ROOTFS}/EFI/BOOT
    install ${DEPLOY_DIR_IMAGE}/systemd-bootx64.efi -T ${IMAGE_ROOTFS}/EFI/BOOT/bootx64.efi
    install ${DEPLOY_DIR_IMAGE}/bzImage ${IMAGE_ROOTFS}

    install -d ${IMAGE_ROOTFS}/loader/entries
    make_loader_conf > ${IMAGE_ROOTFS}/loader/loader.conf
    #make_install_conf > ${IMAGE_ROOTFS}/loader/entries/install.conf
    make_live_conf > ${IMAGE_ROOTFS}/loader/entries/live.conf

    install -d ${IMAGE_ROOTFS}/misc
    make_citadel_conf > ${IMAGE_ROOTFS}/misc/citadel.conf
}

SYSLINUX_MODULES = "ldlinux.c32 menu.c32 libutil.c32 gptmbr.bin"

install_syslinux_files() {
    install -d ${IMAGE_ROOTFS}/syslinux
    make_syslinux_conf > ${IMAGE_ROOTFS}/syslinux/syslinux.cfg
    for m in ${SYSLINUX_MODULES}; do
        install ${RECIPE_SYSROOT_NATIVE}/usr/share/syslinux/$m ${IMAGE_ROOTFS}/syslinux
    done
}

install_image_files() {
    install -d ${IMAGE_ROOTFS}/images
    install_resource_image "rootfs" ${CITADEL_IMAGE_VERSION_rootfs}
    install_resource_image "extra" ${CITADEL_IMAGE_VERSION_extra}
    install_resource_image "kernel" ${CITADEL_IMAGE_VERSION_kernel}
    install ${TOPDIR}/appimg/appimg-rootfs.tar.xz ${IMAGE_ROOTFS}/images/
}

make_loader_conf() {
    echo "default live"
    echo "timeout 5"
}

make_install_conf() {
    echo "title Install Subgraph OS (Citadel)"
    echo "linux /bzImage"
    echo "options ${KERNEL_CMDLINE} citadel.install"
}

make_live_conf() {
    echo "title Run Live Subgraph OS (Citadel)"
    echo "linux /bzImage"
    echo "options ${KERNEL_CMDLINE} citadel.live"
}

CITADEL_KERNEL_CMDLINE = "add_efi_memmap intel_iommu=off cryptomgr.notests rcupdate.rcu_expedited=1 rcu_nocbs=0-64 tsc=reliable no_timer_check noreplace-smp i915.fastboot=1 quiet splash"
make_citadel_conf() {
    echo "title Subgraph OS (Citadel)"
    echo "linux /bzImage"
    echo "options ${CITADEL_KERNEL_CMDLINE}"
}

make_syslinux_conf() {
cat << EOF
UI menu.c32
PROMPT 0

MENU TITLE Boot Live Subgraph OS (Citadel)
TIMEOUT 50
DEFAULT subgraph

LABEL subgraph
        MENU LABEL Subgraph OS
	LINUX ../bzImage
	APPEND ${KERNEL_CMDLINE} citadel.live
EOF
}

install_resource_image() {
    version=$(printf "%03d" ${2})

    if [ "${1}" = "kernel" ]; then
        kversion=$(cat ${DEPLOY_DIR_IMAGE}/kernel.version)
        src_fname="citadel-kernel-${kversion}-${CITADEL_IMAGE_CHANNEL}-${version}.img"
        dst_fname="citadel-kernel-${kversion}.img"
    else
        src_fname="citadel-${1}-${CITADEL_IMAGE_CHANNEL}-${version}.img"
        dst_fname="citadel-${1}.img"
    fi

    install ${DEPLOY_DIR_IMAGE}/${src_fname} -T ${IMAGE_ROOTFS}/images/${dst_fname}
}


CITADEL_BOOT_IMAGE = "${B}/boot.img"
CITADEL_INSTALLER_IMAGE = "${B}/citadel-installer.img"
do_image() {
    write_boot_image ${CITADEL_BOOT_IMAGE}
    write_installer_image ${CITADEL_INSTALLER_IMAGE} ${CITADEL_BOOT_IMAGE} 
}
do_image[umask] = "022"
do_image[dirs] = "${TOPDIR}"
addtask do_image after do_rootfs before do_build

do_deploy() {
    install -m 644 ${CITADEL_INSTALLER_IMAGE} ${DEPLOYDIR}
}

addtask do_deploy after do_image before do_build

#
# write_boot_image [image path] 
#
write_boot_image() {
    IMAGE_PATH=${1}
    BLOCKS_ROOTFS=$(du -bks ${IMAGE_ROOTFS} | cut -f1)
    BLOCKS_EXTRA=2048
    IMAGE_SIZE=$(expr ${BLOCKS_ROOTFS} + ${BLOCKS_EXTRA})

    if [ -e ${IMAGE_PATH} ]; then
        
        rm ${IMAGE_PATH}
    fi

    bbdebug 1 Creating ${IMAGE_SIZE} block msdos image at ${IMAGE_PATH}
    mkdosfs -n boot -C ${IMAGE_PATH} ${IMAGE_SIZE}
    mcopy -i ${IMAGE_PATH} -s ${IMAGE_ROOTFS}/* ::/

    syslinux --directory syslinux --install ${IMAGE_PATH}
}

write_installer_image() {
    INSTALLER_IMAGE=${1}
    BOOT_IMAGE=${2}
    BOOT_IMAGE_SIZE=$(stat -c "%s" ${BOOT_IMAGE})
    BOOT_IMAGE_SECTORS=$(expr ${BOOT_IMAGE_SIZE} / 512)
    TOTAL_SECTORS=$(expr ${BOOT_IMAGE_SECTORS} + 32)
    TOTAL_IMAGE_BLOCKS=$(expr ${TOTAL_SECTORS} / 2)

    if [ -e ${INSTALLER_IMAGE} ]; then
        rm ${INSTALLER_IMAGE}
    fi

    bbdebug 1 Creating ${TOTAL_IMAGE_BLOCKS} block empty image file at ${INSTALLER_IMAGE}
    truncate -s ${TOTAL_IMAGE_BLOCKS}K ${INSTALLER_IMAGE}
    parted -s ${INSTALLER_IMAGE} mklabel msdos

    offset=32
    end=$(expr ${offset} + ${BOOT_IMAGE_SECTORS} - 1)
    bbdebug 1 parted -s ${INSTALLER_IMAGE} unit s mkpart fat32 ${offset} ${end}
    parted -s ${INSTALLER_IMAGE} unit s mkpart primary fat32 ${offset} ${end}
    parted -s ${INSTALLER_IMAGE} set 1 boot on
    bbdebug 1 dd if=${BOOT_IMAGE} of=${INSTALLER_IMAGE} seek=${offset} count=${BOOT_IMAGE_SECTORS} conv=sparse,nocreat,notrunc
    dd if=${BOOT_IMAGE} of=${INSTALLER_IMAGE} seek=${offset} count=${BOOT_IMAGE_SECTORS} conv=sparse,nocreat,notrunc

    dd bs=440 count=1 conv=notrunc if=${RECIPE_SYSROOT_NATIVE}/usr/share/syslinux/mbr.bin of=${INSTALLER_IMAGE}

    parted -s ${INSTALLER_IMAGE} unit s print
}

do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[noexec] = "1"
deltask do_populate_sysroot
do_package[noexec] = "1"
deltask do_package_qa
do_packagedata[noexec] = "1"
do_package_write_deb[noexec] = "1"
