require citadel-kernel.inc

DAPPER_PATCH = "4.9.135-2018-10-26"

SRC_URI = "\
    https://cdn.kernel.org/pub/linux/kernel/v4.x/linux-${LINUX_VERSION}.tar.xz;name=kernel \
    https://github.com/dapperlinux/dapper-secure-kernel-stable/raw/master/dapper-secure-kernel-patchset-${DAPPER_PATCH}.patch;name=patch \
    \
    file://defconfig \
    file://ignore-sysroot-for-plugin-build.patch \
    file://0114-smpboot-reuse-timer-calibration.patch \
    file://0116-Initialize-ata-before-graphics.patch \
    file://0117-WireGuard.patch \
    file://0001-libata-Add-new-med_power_with_dipm-link_power_manage.patch \
"
SRC_URI[kernel.md5sum] = "4d131a974821f35baa784b566b60ba5e"
SRC_URI[kernel.sha256sum] = "e023b0bbe9ea7fc56aa57210342dd18ea3e0900ee207226df1523c6d7df154ce"

SRC_URI[patch.md5sum] = "ebb07d5cc90852c753e677bb2d31f9d9"
SRC_URI[patch.sha256sum] = "529962f3b4d5ffa250f2b90636f1cbb21004fb48e76c2f4d2cc96e2d3fdbb6fe"


