#!/bin/bash

APPIMG_BUILDER_BASE="/usr/share/appimg-builder"
source ${APPIMG_BUILDER_BASE}/common.inc

if [ "$EUID" -ne 0 ]; then
    fatal "The stage-two.sh script is not running as root."
fi

if [ $# -ne 1 ]; then
    fatal "The stage-two.sh script expects a single argument (configuration file path)"
fi

if [ ! -f ${1} ]; then
    fatal "Configuration file '${1}' does not exist."
fi

# running module 'utility-library' replaces this function with a more powerful version
module() {
    local modpath=${APPIMG_BUILDER_BASE}/appimg-modules/${1}
    [[ -f ${modpath} ]] || fatal "Could not find module '${1}'"
    source ${modpath}
}

pre_install_packages() {
    info "Running pre package install modules"
    for mod in ${PRE_INSTALL_MODULES}; do
        module ${mod}
    done
}

post_install_packages() {
    info "Running post package install modules"
    for mod in ${POST_INSTALL_MODULES}; do
        module ${mod}
    done
}

run_build() {
    pre_install_packages
    module install-packages
    post_install_packages
}


# config file imported here
source ${1}

#
# Define any of PACKAGES, PRE_INSTALL_MODULES, POST_INSTALL_MODULES in config file
# to append to the corresponding BASE list.  Also you can override the BASE lists
# entirely by setting the variable name in config file.
#
PACKAGES="${BASE_PACKAGES} ${PACKAGES:-}"
PRE_INSTALL_MODULES="${BASE_PRE_INSTALL_MODULES} ${PRE_INSTALL_MODULES:-}" 
POST_INSTALL_MODULES="${BASE_POST_INSTALL_MODULES} ${POST_INSTALL_MODULES:-}"

run_build
