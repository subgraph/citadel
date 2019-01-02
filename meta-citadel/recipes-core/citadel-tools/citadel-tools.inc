SUMMARY = "${PN}"
HOMEPAGE = "http://github.com/subgraph/citadel"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM="" 

inherit cargo

#
# Update this when changes are pushed to github
#
SRCREV = "58a58e234fb5f1e0e51dff0db3ee86b49e90eddc"

GIT_URI = "git://github.com/subgraph/citadel-tools.git;protocol=https"

#
# To make development more convenient citadel-tools recipes support
# building from a checked out tree on the filesystem. If the variable
# CITADEL_TOOLS_PATH is set (preferably in build/conf/local.conf) then
# bitbake will not check out the source files from git but instead will
# copy the directory this variable contains.
#

#
#
# By default: 
#
#     S = "${WORKDIR}/git/${BPN}", 
#
# if CITADEL_TOOLS_PATH is set:
#
#     S = ${WORKDIR}${CITADEL_TOOLS_PATH}/${BPN}
#
S = "${WORKDIR}${@source_path(d)}/${BPN}"

#
# By default:
#
#     SRC_URI += ${GIT_URI}
#
# If CITADEL_TOOLS_PATH is set:
#
#     SRC_URI += "file://${CITADEL_TOOLS_PATH}"
#
SRC_URI += "${@source_uri(d)}"

# what is this even for?
CARGO_SRC_DIR=""

def source_path(d):
    tools_path = d.getVar("CITADEL_TOOLS_PATH")

    if tools_path:
        return tools_path 
    else:
        return "/git"

def source_uri(d):
    tools_path = d.getVar("CITADEL_TOOLS_PATH")
    if tools_path:
        return "file://" + tools_path
    else:
        return d.getVar("GIT_URI")


