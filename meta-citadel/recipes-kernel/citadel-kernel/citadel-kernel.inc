DESCRIPTION = "Citadel linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

BUILD_LDFLAGS = "-L${STAGING_LIBDIR} \
    -Wl,-rpath-link,${STAGING_LIBDIR} \
    -Wl,-rpath,${STAGING_LIBDIR} \
    -Wl,-O1 \
    -Wl,--dynamic-linker=${STAGING_LIBDIR}/ld-linux-x86-64.so.2"

BUILD_CFLAGS = "-O2 -pipe -v"

inherit kernel 

EXTRA_OEMAKE = 'HOSTCC="x86_64-oe-linux-gcc --sysroot=${RECIPE_SYSROOT}" HOSTCXX="x86_64-oe-linux-g++ --sysroot=${RECIPE_SYSROOT} -isystem ${STAGING_INCDIR_NATIVE}" HOSTCFLAGS="${BUILD_CFLAGS} ${BUILD_LDFLAGS}" HOSTLDFLAGS="--sysroot=${RECIPE_SYSROOT} ${BUILD_LDFLAGS}"'

# if OVERRIDES contains citadel-powertop, then find defconfig in citadel-powertop dirctory
# This config disables CONFIG_GRKERNSEC_KMEM and enables CONFIG_DEBUG_FS so that
# powertop will work
FILESEXTRAPATHS_prepend_citadel-powertop = "${FILE_DIRNAME}/citadel-powertop:"
LINUX_VERSION_EXTENSION_append_citadel-powertop = "-powertop"

LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"
LINUX_VERSION ?= "${PV}"
LINUX_VERSION_EXTENSION_append = "-citadel"

S = "${WORKDIR}/linux-${LINUX_VERSION}"

do_deploy_append() {
    rm ${DEPLOYDIR}/bzImage
    ln -sf bzImage-initramfs-${KERNEL_IMAGE_NAME}.bin ${DEPLOYDIR}/bzImage
    echo "${PV}" > ${DEPLOYDIR}/kernel.version
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

DEPENDS += "libgcc openssl coreutils-native lz4-native"

COMPATIBLE_MACHINE = "(intel-corei7-64)"

