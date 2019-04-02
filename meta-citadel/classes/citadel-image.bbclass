
DEPENDS_append = " citadel-tools-native mtools-native cryptsetup-native coreutils-native"

# Block size must be 4096 or dm-verity won't work
EXTRA_IMAGECMD_ext4 = "-i 4096 -b 4096"
IMAGE_FSTYPES = "ext4"
IMAGE_OVERHEAD_FACTOR = "1.2"

inherit image

CITADEL_IMAGE_CHANNEL ??= "dev"
CITADEL_IMAGE_COMPRESS ??= "true"

do_citadel_mkimage() {
    cat > ${B}/mkimage.conf << EOF
image-type = "${CITADEL_IMAGE_TYPE}"
channel = "${CITADEL_IMAGE_CHANNEL}"
version = ${CITADEL_IMAGE_VERSION}
timestamp = "${DATETIME}"
source = "${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.ext4"
compress = ${CITADEL_IMAGE_COMPRESS}
EOF

    ver=$(printf "%03d" ${CITADEL_IMAGE_VERSION})

    if [ "${CITADEL_IMAGE_TYPE}" = "kernel" ]; then
        KERNEL_ID=$(cat ${DEPLOY_DIR_IMAGE}/kernel.id)
        echo "kernel-version = \"${CITADEL_KERNEL_VERSION}\"" >> ${B}/mkimage.conf
        echo "kernel-id = \"${KERNEL_ID}\"" >> ${B}/mkimage.conf
        fname="citadel-kernel-${CITADEL_KERNEL_VERSION}-${CITADEL_IMAGE_CHANNEL}-${ver}.img"
    else
        fname="citadel-${CITADEL_IMAGE_TYPE}-${CITADEL_IMAGE_CHANNEL}-${ver}.img"
    fi
    citadel-mkimage ${B}
    mv ${B}/${fname} ${IMGDEPLOYDIR}
}

addtask do_citadel_mkimage after do_image_ext4 before do_image_complete
do_citadel_mkimage[cleandirs] = "${B}"
do_citadel_mkimage[vardepsexclude] = "DATETIME"

IMAGE_POSTPROCESS_COMMAND += " generate_shasum_buildhistory ;"

generate_shasum_buildhistory() {
    mkdir -p ${BUILDHISTORY_DIR_IMAGE}
    ( cd ${IMAGE_ROOTFS} && find . -type f -exec sha1sum '{}' \; | sort -k2 > ${BUILDHISTORY_DIR_IMAGE}/image-shasums.txt )
}
