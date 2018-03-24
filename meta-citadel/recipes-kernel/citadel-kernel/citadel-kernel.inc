DESCRIPTION = "Citadel linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

inherit kernel 

# if OVERRIDES contains citadel-powertop, then find defconfig in citadel-powertop dirctory
# This config disables CONFIG_GRKERNSEC_KMEM and enables CONFIG_DEBUG_FS so that
# powertop will work
FILESEXTRAPATHS_prepend_citadel-powertop = "${FILE_DIRNAME}/citadel-powertop:"
LINUX_VERSION_EXTENSION_append_citadel-powertop = "-powertop"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"
LINUX_VERSION ?= "${PV}"
LINUX_VERSION_EXTENSION_append = "-citadel"

SRC_URI = "https://cdn.kernel.org/pub/linux/kernel/v4.x/linux-${LINUX_VERSION}.tar.xz;name=kernel"


SRC_URI += "https://github.com/minipli/linux-unofficial_grsec/releases/download/v${LINUX_VERSION}-unofficial_grsec/v${LINUX_VERSION}-unofficial_grsec-${PATCH_DATE_TAG}.diff;name=patch"

SRC_URI += " \
    file://defconfig \
    file://ignore-sysroot-for-plugin-build.patch \
    file://0114-smpboot-reuse-timer-calibration.patch \
    file://0116-Initialize-ata-before-graphics.patch \
    "

S = "${WORKDIR}/linux-${LINUX_VERSION}"

do_deploy_append() {
    rm ${DEPLOYDIR}/bzImage
    ln -sf bzImage-initramfs${KERNEL_IMAGE_BASE_NAME}.bin ${DEPLOYDIR}/bzImage
}

#
# Replaces function with same name in kernel.bbclass since that implementation
# doesn't pass destination argument to lz4 in which case the decompressed output
# just disappears into thin air it seems.
#
copy_initramfs() {
    echo "copy_initramfs override"
    mkdir -p ${B}/usr
    rm -f ${B}/usr/${INITRAMFS_IMAGE_NAME}.cpio
    cp ${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME}.cpio.lz4 ${B}/usr/.
    lz4 -df ${B}/usr/${INITRAMFS_IMAGE_NAME}.cpio.lz4 ${B}/usr/${INITRAMFS_IMAGE_NAME}.cpio
    ls -al ${B}/usr
    echo "Finished copy of initramfs into ./usr"
}

# Don't install kernel into images, see kernel.bbclass
RDEPENDS_${KERNEL_PACKAGE_NAME}-base = ""

#
# https://github.com/96boards/meta-96boards/issues/81#issuecomment-311256044
# https://github.com/96boards/meta-96boards/blob/master/recipes-kernel/linux/linux-96boards_4.4.bb
#
# coreutils for /usr/bin/seq which pax size overflow gcc plugin uses

DEPENDS += "openssl-native coreutils-native lz4-native"
HOST_EXTRACFLAGS += "-I${STAGING_INCDIR_NATIVE}"

COMPATIBLE_MACHINE = "(intel-corei7-64)"

