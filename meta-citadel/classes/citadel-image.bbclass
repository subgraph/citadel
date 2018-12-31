
DEPENDS_append = " citadel-image-native mtools-native"

# Block size must be 4096 or dm-verity won't work
EXTRA_IMAGECMD_ext2 = "-i 4096 -b 4096"
IMAGE_FSTYPES = "ext2"

inherit image

CITADEL_IMAGE_CHANNEL ??= "dev"

do_citadel_mkimage() {
    cat > ${B}/mkimage.conf << EOF
image-type = "${CITADEL_IMAGE_TYPE}"
channel = "${CITADEL_IMAGE_CHANNEL}"
version = ${CITADEL_IMAGE_VERSION}
source = "${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ext2"
EOF

    ver=$(printf "%03d" ${CITADEL_IMAGE_VERSION})

    if [ "${CITADEL_IMAGE_TYPE}" = "modules" ]; then
        echo "kernel-version = \"${CITADEL_KERNEL_VERSION}\"" >> ${B}/mkimage.conf
        fname="citadel-modules-${CITADEL_KERNEL_VERSION}-${CITADEL_IMAGE_CHANNEL}-${ver}.img"
    else
        fname="citadel-${CITADEL_IMAGE_TYPE}-${CITADEL_IMAGE_CHANNEL}-${ver}.img"
    fi
    citadel-image build ${B}
    mv ${B}/${fname} ${IMGDEPLOYDIR}
}

addtask do_citadel_mkimage after do_image_ext2 before do_image_complete
