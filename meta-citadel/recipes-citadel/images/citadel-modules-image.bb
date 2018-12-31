
DESCRIPTION = "Kernel modules"
LICENSE = "MIT"

PACKAGE_INSTALL = "kernel-modules"

CITADEL_IMAGE_VERSION = "${CITADEL_IMAGE_VERSION_modules}"
CITADEL_IMAGE_TYPE = "modules"

require citadel-image.inc
inherit citadel-image

ROOTFS_POSTPROCESS_COMMAND += "write_manifest_file; "

write_manifest_file() {
    echo "/usr/lib/modules" > ${IMAGE_ROOTFS}/manifest
}

