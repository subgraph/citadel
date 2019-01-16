
FILESEXTRAPATHS_prepend := "${THISDIR}/meson:"

# disable-rpath-handling:
#   https://github.com/mesonbuild/meson/issues/2567
#   https://github.com/openembedded/openembedded-core/commit/29b5ef236914b152fd255e134569d4a177656bb0
SRC_URI += "\
    file://0001-use-exe-wrapper-for-custom-targets.patch \
    file://disable-rpath-handling.patch \
"
