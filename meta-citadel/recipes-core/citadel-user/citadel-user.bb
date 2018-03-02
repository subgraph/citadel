inherit allarch useradd

LICENSE = "MIT"
S = "${WORKDIR}"

ALLOW_EMPTY_${PN} = "1"
USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "-m -u 1000 -s /bin/bash citadel"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
