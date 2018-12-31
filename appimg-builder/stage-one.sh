#!/bin/bash

: ${APPIMG_BUILDER_BASE:="/usr/share/appimg-builder"}
source ${APPIMG_BUILDER_BASE}/common.inc

umount_if_tmpfs() {
    if findmnt -t tmpfs -M ${1} > /dev/null; then
        info "unmounting existing rootfs tmpfs"
        umount ${1}
    fi
}

setup_rootfs() {
    umount_if_tmpfs ${ROOTFS}
    if [[ -d ${ROOTFS} ]]; then
        info "Removing existing rootfs at ${ROOTFS}"
        rm -rf ${ROOTFS}
    fi

    mkdir --parents ${ROOTFS}

    if [[ ${USE_TMPFS} -eq 1 ]]; then
        mount -t tmpfs rootfs-tmp ${ROOTFS}
    fi
    chmod 0755 ${ROOTFS}
}

run_debootstrap() {

    [[ -f ${CACHE_DIR}/lock ]] && rm -f ${CACHE_DIR}/lock
    mkdir --parents ${CACHE_DIR} ${ROOTFS}/var/cache/apt/archives

    info "Bind mounting ${CACHE_DIR} to ${ROOTFS}/var/cache/apt/archives"
    mount --bind ${CACHE_DIR} ${ROOTFS}/var/cache/apt/archives

    info "Launching debootstrap"

    debootstrap --verbose --merged-usr --variant=minbase \
        --include=systemd-sysv,locales \
        ${DEBIAN_RELEASE} ${ROOTFS} ${DEBIAN_MIRROR}
}

setup_chroot() {

    mount chproc ${ROOTFS}/proc -t proc
    mount chsys ${ROOTFS}/sys -t sysfs
    mount chtmp ${ROOTFS}/tmp -t tmpfs

    # Install a copy of appimg-builder inside new image
    mkdir -p ${ROOTFS}/usr/share
    cp -a ${APPIMG_BUILDER_BASE} ${ROOTFS}/usr/share
    ln -s /usr/share/appimg-builder/stage-one.sh ${ROOTFS}/usr/bin/appimg-builder

    # $BUILDFILE and any extra files go in /tmp/appimg-build of rootfs
    mkdir -p ${ROOTFS}/tmp/appimg-build
    cp ${BUILDFILE} ${ROOTFS}/tmp/appimg-build/build.conf
}

cleanup_chroot() {
    umount ${ROOTFS}/proc
    umount ${ROOTFS}/sys
    umount ${ROOTFS}/tmp
    umount ${ROOTFS}/var/cache/apt/archives

    # Remove cache files in case we are creating a tarball for distribution
    rm -f ${ROOTFS}/var/cache/apt/pkgcache.bin
    rm -f ${ROOTFS}/var/cache/apt/srcpkgcache.bin
}

run_chroot_stage() {

    setup_chroot

    #
    # Run second-stage.sh inside chroot(), pass in various environment variables
    #

    DEBIAN_FRONTEND=noninteractive \
        DEBCONF_NONINTERACTIVE_SEEN=true \
        LC_ALL=C LANGUAGE=C LANG=C \
        DEBIAN_RELEASE=${DEBIAN_RELEASE} DEBIAN_MIRROR=${DEBIAN_MIRROR} \
        chroot ${ROOTFS} /usr/share/appimg-builder/stage-two.sh /tmp/appimg-build/build.conf

    info "chroot installation stage finished, cleaning chroot setup"
    cleanup_chroot
}

generate_tarball() {
    local tarball=${WORKDIR}/appimg-rootfs.tar

    info "----- Generating rootfs tarball -----"
    tar -C ${ROOTFS} --numeric-owner -c --xattrs --xattrs-include=* -f $tarball .
    if [[ ${DO_XZ} -eq 1 ]]; then
        info "Compressing $tarball"
        xz --force --threads=0 $tarball
        tarball=${tarball}.xz
    fi
    echo
    ls -hl $tarball
    echo
}

usage() {
cat <<-EOF
USAGE: appimg-builder [options] [config-file]

OPTIONS

    --new               Create a configuration file template called build.conf in the current directory
    -d <directory>      Choose a non-default directory for build output (currently: $(pwd)/appimg)
    -t                  Create a tarball but don't compress it
    -z                  Create a tarball compressed with xz
    --no-tmpfs          Do not use tmpfs as rootfs build directory
    --no-confirm        Do not ask for confirmation before beginning

For more documentation see /usr/share/appimg-builder/README

EOF
exit 0
}

ask_confirm() {
    local use_tmpfs="No"
    [[ ${USE_TMPFS} -eq 1 ]] && use_tmpfs="Yes"

    printf "About to build application image with the following parameters:\n\n"
    printf "\tBuild Configuration File : ${BUILDFILE}\n"
    printf "\tOutput rootfs directory  : ${ROOTFS}\n"
    printf "\tBuild rootfs on tmpfs    : ${use_tmpfs}\n"
    [[ -e ${ROOTFS} ]] && printf "\nWarning: rootfs directory from a prior build exists and will be deleted before building new image\n\n"

    read -p "Ok to proceed? (y/N): " confirm

    [[ $confirm =~ ^[Yy] ]] || exit 0
}

try_config() {
    local rp
    rp=$(realpath ${1} 2> /dev/null) || return 1
    [[ -f ${rp} ]] || return 1
    printf "${rp}"
}
    
WORKDIR="$(pwd)/appimg"

DO_TAR=0
DO_XZ=0
USE_TMPFS=1
NO_CONFIRM=0

while [[ $# -gt 0 ]]; do
    key=${1}
    case $key in
        -d)
            mkdir -p "${2}"
            WORKDIR="$(realpath ${2})"
            shift
            shift
            ;;

        -t)
            DO_TAR=1
            shift
            ;;

        -z)
            DO_TAR=1 DO_XZ=1
            shift
            ;;

        --no-tmpfs)
            USE_TMPFS=0
            shift
            ;;

        --no-confirm)
            NO_CONFIRM=1
            shift
            ;;

        --new)
            cp --verbose ${APPIMG_BUILDER_BASE}/build-template.conf build.conf
            exit 0
            ;;

        --help|-h|help)
            usage
            ;;
        -*)
            printf "Unknown option ${key}\n"
            usage
            ;;

        *)
            BUILDFILE=$(try_config ${1}) || fatal "Cannot find config file '${1}'"
            shift
            ;;
    esac
done

if [ "$EUID" -ne 0 ]; then
    echo "The appimg-builder must be run with root privileges."
    exit 1
fi

if [[ -z ${BUILDFILE} ]]; then
    BUILDFILE=$(try_config "${PWD}/build.conf" || try_config "${APPIMG_BUILDER_BASE}/basic-image.conf") || fatal "Could not find a configuration file to use"
fi

ROOTFS=${WORKDIR}/rootfs
CACHE_DIR=${WORKDIR}/var-cache-apt-archives

[[ ${NO_CONFIRM} -ne 1 ]] && ask_confirm

# black magick from stack overflow
exec > >(tee -a $WORKDIR/appimg-build.log) 2>&1

info "Starting build of application image from configuration file ${BUILDFILE}"

source ${BUILDFILE}

setup_rootfs

run_debootstrap

run_chroot_stage

info "rootfs build is completed:"
info "    $(du -sh ${ROOTFS})"

if [[ ${DO_TAR} -eq 1 ]]; then
    generate_tarball
fi

if [[ ${USE_TMPFS} -eq 1 ]]; then
    info "Root directory is a tmpfs. To remove mount and directory contents:"
    info "    sudo umount ${ROOTFS}"
fi
