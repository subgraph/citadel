DESCRIPTION = "Subgraph OS Citadel minimal image builder"
LICENSE = "MIT"

IMAGE_FEATURES_append = " empty-root-password"

IMAGE_INSTALL = "\
    packagegroup-core-boot \
    packagegroup-core-full-cmdline-utils \
    "

ROOT_PASSWORD ?= "citadel"

inherit core-image

WKS_FILE="citadel-image-minimal.wks"
DEPENDS += "linux-citadel"

