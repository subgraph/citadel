# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   protos/wayland-protocols/COPYING
#   lib/chck/LICENSE
#
# NOTE: multiple licenses have been detected; they have been separated with &
# in the LICENSE value for now since it is a reasonable assumption that all
# of the licenses apply. If instead there is a choice between the multiple
# licenses then you should change the value to separate the licenses with |
# instead of &. If there is any doubt, check the accompanying documentation
# to determine which situation is applicable.
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0424d0715413cefdd4fbd497e682a87c \
                    file://protos/wayland-protocols/COPYING;md5=c7b12b6702da38ca028ace54aae3d484 \
                    file://lib/chck/LICENSE;md5=8a56f5f1791f1100ec6d74f74bd847ca"

SRC_URI = "gitsm://github.com/Cloudef/wlc"

# Modify these as desired
PV = "1.0+git${SRCPV}"
SRCREV = "6542c16652df147523245fc547d2a5ff4088a0cb"

S = "${WORKDIR}/git"

# NOTE: unable to map the following CMake package dependencies: LibInput Math Elogind GBM WaylandProtocols GLESv2 Pixman Systemd Wayland XKBCommon Udev
DEPENDS = "libx11 libdrm dbus libxcb xcb-util-wm xcb-util-image virtual/egl mesa wayland wayland-native libxkbcommon libinput systemd pixman"

inherit cmake

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = ""

