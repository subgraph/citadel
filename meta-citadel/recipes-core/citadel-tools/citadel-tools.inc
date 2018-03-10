HOMEPAGE = "http://github.com/subgraph/citadel"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM="" 

# what is this even for?
CARGO_SRC_DIR=""

S = "${WORKDIR}/${PN}"
do_unpack_src() {
    rm -rf ${WORKDIR}/${PN}
    cp -a ${TOPDIR}/../citadel-tools/${PN} ${WORKDIR}
    rm -rf ${WORKDIR}$/${PN}/target
}
# don't use cached task
#do_unpack_src[nostamp] = "1"
addtask unpack_src after do_unpack before do_patch
