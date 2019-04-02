SUMMARY = "${PN}"
HOMEPAGE = "http://github.com/subgraph/citadel"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM=""

inherit cargo systemd
#
# Update this when changes are pushed to github
#
SRCREV = "b38c7cb9431417319c957ed26409f15619320912"

GIT_URI = "git://github.com/subgraph/citadel-tools.git;protocol=https"

# If Cargo.lock changes in citadel-tools, this needs to be updated.
# cargo bitbake does not support workspaces so as a workaround first
# copy the Cargo.lock file into one of the tool subdirectories. In
# that subdirectory run "cargo bitbake" and it will produce a bitbake
# recipe file with the correct set of dependencies for the Cargo.lock
# file.  Copy just the SRC_URI variable from that file here to update
# the dependency list.

SRC_URI += " \
crate://crates.io/adler32/1.0.3 \
crate://crates.io/ansi_term/0.11.0 \
crate://crates.io/arc-swap/0.3.8 \
crate://crates.io/array-macro/1.0.3 \
crate://crates.io/atty/0.2.11 \
crate://crates.io/autocfg/0.1.2 \
crate://crates.io/backtrace-sys/0.1.28 \
crate://crates.io/backtrace/0.3.14 \
crate://crates.io/base64/0.10.1 \
crate://crates.io/bincode/1.0.1 \
crate://crates.io/bitflags/1.0.4 \
crate://crates.io/block-buffer/0.7.0 \
crate://crates.io/block-padding/0.1.3 \
crate://crates.io/byte-tools/0.3.1 \
crate://crates.io/byteorder/1.3.1 \
crate://crates.io/bzip2-sys/0.1.7 \
crate://crates.io/bzip2/0.3.3 \
crate://crates.io/cc/1.0.32 \
crate://crates.io/cfg-if/0.1.7 \
crate://crates.io/chrono/0.4.6 \
crate://crates.io/clap/2.32.0 \
crate://crates.io/crc32fast/1.2.0 \
crate://crates.io/crossbeam-channel/0.3.8 \
crate://crates.io/crossbeam-utils/0.6.5 \
crate://crates.io/cursive/0.11.0 \
crate://crates.io/dbus/0.6.4 \
crate://crates.io/digest/0.8.0 \
crate://crates.io/enum-map-derive/0.4.1 \
crate://crates.io/enum-map-internals/0.1.2 \
crate://crates.io/enum-map/0.5.0 \
crate://crates.io/enumset/0.3.16 \
crate://crates.io/enumset_derive/0.2.0 \
crate://crates.io/failure/0.1.5 \
crate://crates.io/failure_derive/0.1.5 \
crate://crates.io/fake-simd/0.1.2 \
crate://crates.io/filetime/0.2.4 \
crate://crates.io/generic-array/0.12.0 \
crate://crates.io/hex/0.3.2 \
crate://crates.io/http_req/0.4.6 \
crate://crates.io/inotify-sys/0.1.3 \
crate://crates.io/inotify/0.7.0 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/lazy_static/1.3.0 \
crate://crates.io/libc/0.2.51 \
crate://crates.io/libdbus-sys/0.1.5 \
crate://crates.io/libflate/0.1.21 \
crate://crates.io/libsodium-sys/0.2.1 \
crate://crates.io/log/0.4.6 \
crate://crates.io/nix/0.12.0 \
crate://crates.io/num-complex/0.2.1 \
crate://crates.io/num-integer/0.1.39 \
crate://crates.io/num-iter/0.1.37 \
crate://crates.io/num-rational/0.2.1 \
crate://crates.io/num-traits/0.2.6 \
crate://crates.io/num/0.2.0 \
crate://crates.io/opaque-debug/0.2.2 \
crate://crates.io/owning_ref/0.4.0 \
crate://crates.io/pkg-config/0.3.14 \
crate://crates.io/podio/0.1.6 \
crate://crates.io/proc-macro2/0.4.27 \
crate://crates.io/quote/0.6.11 \
crate://crates.io/redox_syscall/0.1.51 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/ring/0.14.6 \
crate://crates.io/rpassword/2.1.0 \
crate://crates.io/rustc-demangle/0.1.13 \
crate://crates.io/rustls/0.15.1 \
crate://crates.io/same-file/1.0.4 \
crate://crates.io/sct/0.5.0 \
crate://crates.io/serde/1.0.89 \
crate://crates.io/serde_derive/1.0.89 \
crate://crates.io/sha2/0.8.0 \
crate://crates.io/signal-hook/0.1.8 \
crate://crates.io/smallvec/0.6.9 \
crate://crates.io/sodiumoxide/0.2.1 \
crate://crates.io/spin/0.5.0 \
crate://crates.io/stable_deref_trait/1.1.1 \
crate://crates.io/strsim/0.7.0 \
crate://crates.io/syn/0.14.9 \
crate://crates.io/syn/0.15.29 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/tar/0.4.22 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/textwrap/0.10.0 \
crate://crates.io/time/0.1.42 \
crate://crates.io/toml/0.4.10 \
crate://crates.io/typenum/1.10.0 \
crate://crates.io/unicode-segmentation/1.2.1 \
crate://crates.io/unicode-width/0.1.5 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/untrusted/0.6.2 \
crate://crates.io/vcpkg/0.2.6 \
crate://crates.io/vec_map/0.8.1 \
crate://crates.io/void/1.0.2 \
crate://crates.io/walkdir/2.2.7 \
crate://crates.io/webpki-roots/0.16.0 \
crate://crates.io/webpki/0.19.1 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-util/0.1.2 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi/0.3.6 \
crate://crates.io/xattr/0.2.2 \
crate://crates.io/xi-unicode/0.1.0 \
crate://crates.io/zip/0.5.1 \
"

DEPENDS = "libsodium openssl dbus"
BBCLASSEXTEND = "native"
PACKAGES =+ "${PN}-realms ${PN}-tools ${PN}-mkimage"

FILES_${PN}-realms = "${bindir}/realms"
FILES_${PN}-mkimage = "${bindir}/citadel-mkimage"

FILES_${PN} = "\
    ${libexecdir}/citadel-tool \
    ${libexecdir}/citadel-boot \
    ${libexecdir}/citadel-run \
    ${libexecdir}/citadel-install \
    ${libexecdir}/citadel-desktop-sync \
    ${bindir}/citadel-image \
    ${bindir}/citadel-realmfs \
    ${systemd_system_unitdir} \
"

SYSTEMD_SERVICE_${PN} = "citadel-current-watcher.path"

TARGET_BIN = "${B}/target/${CARGO_TARGET_SUBDIR}"

do_install() {
    install -d ${D}${bindir}
    install -d ${D}${libexecdir}
    install -d ${D}${systemd_system_unitdir}

    # Services desktop sync
    install -m 644 ${B}/systemd/citadel-desktop-watcher.path ${D}${systemd_system_unitdir}
    install -m 644 ${B}/systemd/citadel-desktop-watcher.service ${D}${systemd_system_unitdir}
    install -m 644 ${B}/systemd/citadel-current-watcher.path ${D}${systemd_system_unitdir}
    install -m 644 ${B}/systemd/citadel-current-watcher.service ${D}${systemd_system_unitdir}


    # /usr/libexec/citadel-tool
    install -m 755 ${TARGET_BIN}/citadel-tool ${D}${libexecdir}

    # citadel-realms as /usr/bin/realms
    install -m 755 -T ${TARGET_BIN}/citadel-realms ${D}${bindir}/realms

    ln ${D}${libexecdir}/citadel-tool ${D}${libexecdir}/citadel-boot
    ln ${D}${libexecdir}/citadel-tool ${D}${libexecdir}/citadel-install
    ln ${D}${libexecdir}/citadel-tool ${D}${libexecdir}/citadel-desktop-sync
    ln ${D}${libexecdir}/citadel-tool ${D}${libexecdir}/citadel-run
    ln ${D}${libexecdir}/citadel-tool ${D}${bindir}/citadel-image
    ln ${D}${libexecdir}/citadel-tool ${D}${bindir}/citadel-mkimage
    ln ${D}${libexecdir}/citadel-tool ${D}${bindir}/citadel-realmfs
}

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
#     S = "${WORKDIR}/git"
#
# if CITADEL_TOOLS_PATH is set:
#
#     S = "${WORKDIR}${CITADEL_TOOLS_PATH}"
#
S = "${WORKDIR}${@source_path(d)}"

#
# By default:
#
#     SRC_URI += "${GIT_URI}"
#
# If CITADEL_TOOLS_PATH is set:
#
#     SRC_URI += "file://${CITADEL_TOOLS_PATH}"
#
SRC_URI += "${@source_uri(d)}"

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

# Set debug build if CITADEL_TOOLS_PATH is set for faster builds
DEBUG_BUILD = "${@debug_build(d)}"
def debug_build(d):
    tools_path = d.getVar("CITADEL_TOOLS_PATH")
    if tools_path:
        return "1"
    else:
        return "0"

#do_fetch[file-checksums] = ""
