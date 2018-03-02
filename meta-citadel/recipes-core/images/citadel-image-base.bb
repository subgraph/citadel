SUMMARY = "Subgraph OS Citadel Base CLI image builder"
LICENSE = "MIT"


IMAGE_FEATURES_append = " empty-root-password"

#
# Set by the machine configuration with packages essential for device bootup
#
MACHINE_ESSENTIAL_EXTRA_RDEPENDS ?= ""
MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS ?= ""

IMAGE_INSTALL = "\
    packagegroup-citadel-base \
"

RDEPENDS_${PN} = "\
    ${MACHINE_ESSENTIAL_EXTRA_RDEPENDS} \
"

RRECOMMENDS_${PN} = "\
    ${MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS}"

ROOT_PASSWORD ?= "citadel"

inherit core-image

WKS_FILE="citadel-image-minimal.wks"

