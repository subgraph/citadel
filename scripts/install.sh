#!/bin/bash

set -e
set -u

blkdev_info() {
    local model=$(< /sys/block/${1}/device/model)
    local size=$(printf "%sG" $(( $(</sys/block/${1}/size) >> 21 )))
    printf "   Device: /dev/${1}\n"
    printf "     Size: ${size}\n"
    printf "    Model: ${model}\n"
}

errormsg() {
    printf "Failed: ${1}\n" 
    exit 1
}

info() {
    printf "[+] ${1}\n"
}

passphrase=""

ask_passphrase() {
    local p1 p2
    for i in {1..3}
    do
        read -s -p "Enter passphrase for disk encryption: " p1
        echo
        read -s -p "                  Confirm passphrase: " p2
        echo
        
        if [[ ${p1} != ${p2} ]]; then
            printf "THe passphrases did not match\n"
        elif [[ -z ${p1} ]] ; then
            printf "Passphrase cannot be empty\n"
        else
            passphrase=${p1}
            return
        fi
    done
    errormsg "Too many attempts, Unable to set disk encryption passphrase"
}

confirm_device() {
    if [[ ! -b ${1} ]]; then
        errormsg "No block device '${1}' found"
    fi

    local base=$(basename ${1})

    if [[ ! -e /sys/block/${base}/device ]]; then
        errormsg "Unable to find device path /sys/block/${base}/device"
    fi

    printf "Are you sure you want to overwrite this device\n\n%s\n\n" "$(blkdev_info ${base})"
    read -p "Type YES (uppercase) to continue: " confirm
    if [[ ${confirm} != "YES" ]]; then
        echo "Install not confirmed, exiting."
        exit 1
    fi
}

LUKS_UUID="683a17fc-4457-42cc-a946-cde67195a101"

partition_device() {
    local PARTED="parted -a optimal ${1}"
    ${PARTED} -s mklabel gpt
    ${PARTED} mkpart boot fat32 0% 512MiB 
    ${PARTED} set 1 boot on
    ${PARTED} mkpart data ext4 512MiB 100%
    ${PARTED} set 2 lvm on
}

setup_luks() {
    # /dev/sdb2
    local TARGET_LVM=${1}2
    printf "${passphrase}" | cryptsetup -q --uuid=${LUKS_UUID} luksFormat ${TARGET_LVM} -
    printf "${passphrase}" | cryptsetup open --type luks --key-file - ${TARGET_LVM} luks-install
}

setup_lvm() {
    pvcreate -ff --yes /dev/mapper/luks-install
    vgcreate --yes citadel /dev/mapper/luks-install
    lvcreate --yes --size 2g --name rootfsA citadel
    lvcreate --yes --size 2g --name rootfsB citadel
    lvcreate --yes --extents 100%VG --name storage citadel
}

setup_disk() {
    [[ $# -ne 1 ]] && usage
    confirm_device ${1}
    ask_passphrase

    info "Deactivating device ${1}"
    blkdeactivate ${1} >> install.log 2>&1

    info "Partitioning device ${1}"
    partition_device ${1} >> install.log 2>&1

    info "Setting up LUKS disk encryption on partition ${1}2"
    setup_luks ${1} >> install.log 2>&1

    info "Creating LVM volumes inside LUKS volume"
    setup_lvm  >> install.log 2>&1

    info "Creating vfat filesystem on EFI system partition ${1}1"
    mkfs.vfat -F 32 ${1}1 >> install.log 2>&1

    info "Creating btrfs filesystem on storage volume"
    mkfs.btrfs /dev/mapper/citadel-storage >> install.log 2>&1

    lsblk -o NAME,SIZE,TYPE,FSTYPE ${1} >> install.log

}

unmount_disk() {
    info "Closing LVM volumes"
    vgchange -an citadel >> install.log 2>&1
    info "Closing LUKS volume"
    cryptsetup luksClose luks-install
}

install() {
    local MNT="install-mnt"
    mkdir -p install-mnt
    info "Mounting EFI system partition ${1}1"
    mount ${1}1 install-mnt
    info "Installing boot tree to EFI system partition"
    cp -R boot/* install-mnt
    info "Unmounting EFI system partition"
    umount ${1}1

    local PRIMARY_HOME="${MNT}/user-data/primary-home"
    local PRIMARY_ROOTFS="${MNT}/appimg/primary/rootfs"

    info "Mounting storage partition"
    mount /dev/mapper/citadel-storage ${MNT}

    info "Installing base appimg tree"
    mkdir -p ${PRIMARY_ROOTFS}
    ln -s primary ${MNT}/appimg/default.appimg
    tar -C ${PRIMARY_ROOTFS} -xf components/user-rootfs.tar.xz

    mkdir -p ${PRIMARY_HOME}
    cp components/howto.md ${PRIMARY_HOME}
    cp ${PRIMARY_ROOTFS}/home/user/{.bashrc,.profile} ${PRIMARY_HOME}
    chown -R 1000:1000 ${PRIMARY_HOME}

    info "Unmounting storage partition"
    umount /dev/mapper/citadel-storage

    info "Writing citadel image to rootfsA partition"
    dd if=components/citadel-image-rootfs.ext2 of=/dev/mapper/citadel-rootfsA bs=4M >> install.log 2>&1

    #info "Writing citadel image to rootfsB partition"
    #dd if=components/citadel-image-rootfs.ext2 of=/dev/mapper/citadel-rootfsB bs=4M >> install.log 2>&1
}

usage() {
    printf "Usage:\n"
    printf "\t\t./install.sh [<block device>]\n\n"
    exit 1
}


if [[ $# -eq 0 ]]; then
    usage
fi

setup_disk ${1}
install ${1}
unmount_disk
sync
info "Install completed successfully"


