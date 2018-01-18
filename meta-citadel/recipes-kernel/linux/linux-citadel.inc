DESCRIPTION = "Citadel linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"

inherit kernel 


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

# Don't install kernel into images, see kernel.bbclass
RDEPENDS_kernel-base = ""

#
# https://github.com/96boards/meta-96boards/issues/81#issuecomment-311256044
# https://github.com/96boards/meta-96boards/blob/master/recipes-kernel/linux/linux-96boards_4.4.bb
#
# coreutils for /usr/bin/seq which pax size overflow gcc plugin uses

DEPENDS += "openssl-native coreutils-native"
HOST_EXTRACFLAGS += "-I${STAGING_INCDIR_NATIVE}"

COMPATIBLE_MACHINE = "(intel-corei7-64)"

