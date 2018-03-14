FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
# https://github.com/karelzak/util-linux/issues/543
SRC_URI += "file://ioctl_fix.patch"
