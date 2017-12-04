DESCRIPTION = "Image for rootfs"

PACKAGE_INSTALL = "initramfs-framework-base initramfs-module-udev initramfs-module-setup-live ${VIRTUAL-RUNTIME_base-utils} udev base-passwd ${ROOTFS_BOOTSTRAP_INSTALL} linux-firmware-i915 kernel-modules"

IMAGE_FEATURES = ""
export IMAGE_BASENAME = "citadel-initramfs"
IMAGE_LINGUAS = ""

LICENSE = "MIT"

INITRAMFS_MAXSIZE = "512000"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit core-image

IMAGE_ROOTFS_SIZE = "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
