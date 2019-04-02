DESCRIPTION = "Base RealmFS image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
DEPENDS = "citadel-tools-native cryptsetup-native"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit deploy
require citadel-image.inc

REALMFS_DIR = "${TOPDIR}/realmfs"
CITADEL_IMAGE_VERSION = "1"
do_realmfs_mkimage() {
    cat > ${B}/mkimage.conf << EOF
image-type = "realmfs"
channel = "${CITADEL_IMAGE_CHANNEL}"
version = 1
timestamp = "${DATETIME}"
source = "${REALMFS_DIR}/citadel-realmfs.ext4"
realmfs-name = "base"
compress = true
EOF
    citadel-mkimage ${B}
}

addtask do_realmfs_mkimage after do_configure before do_build
do_realmfs_mkimage[vardepsexclude] = "DATETIME"
do_realmfs_mkimage[cleandirs] = "${B}"

do_deploy() {
    ver=$(printf "%03d" ${CITADEL_IMAGE_VERSION})
    fname="citadel-realmfs-${CITADEL_IMAGE_CHANNEL}-${ver}.img"
    install -m 644 -T ${B}/${fname} ${DEPLOYDIR}/base-realmfs.img
}
addtask do_deploy after do_realmfs_mkimage before do_build

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
