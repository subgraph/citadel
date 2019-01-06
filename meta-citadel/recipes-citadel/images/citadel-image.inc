
CITADEL_IMAGE_CHANNEL = "dev"

CITADEL_IMAGE_VERSION_rootfs = "1"
CITADEL_IMAGE_VERSION_extra = "1"
CITADEL_IMAGE_VERSION_kernel = "1"

CITADEL_KERNEL_VERSION = "4.9.65"

generate_kernel_id() {
    sha256sum ${DEPLOY_DIR_IMAGE}/bzImage-intel-corei7-64.bin | cut -d' ' -f1
}
