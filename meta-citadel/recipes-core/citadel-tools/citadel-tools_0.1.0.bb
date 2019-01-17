SUMMARY = "${PN}"
HOMEPAGE = "http://github.com/subgraph/citadel"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM=""

inherit cargo
#
# Update this when changes are pushed to github
#
SRCREV = "ff115e6005247b0b860a2da2b2bd6a0cca37381b"

GIT_URI = "git://github.com/subgraph/citadel-tools.git;protocol=https"

# If Cargo.lock changes in citadel-tools, this needs to be updated.
# cargo bitbake does not support workspaces so as a workaround first
# copy the Cargo.lock file into one of the tool subdirectories. In
# that subdirectory run "cargo bitbake" and it will produce a bitbake
# recipe file with the correct set of dependencies for the Cargo.lock
# file.  Copy just the SRC_URI variable from that file here to update
# the dependency list.

SRC_URI += " \
crate://crates.io/aho-corasick/0.6.9 \
crate://crates.io/ansi_term/0.11.0 \
crate://crates.io/atty/0.2.11 \
crate://crates.io/autocfg/0.1.2 \
crate://crates.io/backtrace-sys/0.1.28 \
crate://crates.io/backtrace/0.3.13 \
crate://crates.io/bitflags/1.0.4 \
crate://crates.io/byteorder/1.2.7 \
crate://crates.io/bytes/0.4.11 \
crate://crates.io/cc/1.0.28 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/clap/2.32.0 \
crate://crates.io/env_logger/0.5.13 \
crate://crates.io/failure/0.1.5 \
crate://crates.io/failure_derive/0.1.5 \
crate://crates.io/futures/0.1.25 \
crate://crates.io/gcc/0.3.55 \
crate://crates.io/humantime/1.2.0 \
crate://crates.io/inotify-sys/0.1.3 \
crate://crates.io/inotify/0.5.1 \
crate://crates.io/iovec/0.1.2 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/lazy_static/1.2.0 \
crate://crates.io/libc/0.2.47 \
crate://crates.io/log/0.4.6 \
crate://crates.io/memchr/2.1.2 \
crate://crates.io/nix/0.10.0 \
crate://crates.io/nix/0.12.0 \
crate://crates.io/proc-macro2/0.4.25 \
crate://crates.io/quick-error/1.2.2 \
crate://crates.io/quote/0.6.10 \
crate://crates.io/redox_syscall/0.1.50 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/regex-syntax/0.6.4 \
crate://crates.io/regex/1.1.0 \
crate://crates.io/ring/0.13.5 \
crate://crates.io/rpassword/2.1.0 \
crate://crates.io/rustc-demangle/0.1.13 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/same-file/1.0.4 \
crate://crates.io/serde/1.0.84 \
crate://crates.io/serde_derive/1.0.84 \
crate://crates.io/strsim/0.7.0 \
crate://crates.io/syn/0.15.26 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/termcolor/0.3.6 \
crate://crates.io/termcolor/1.0.4 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/textwrap/0.10.0 \
crate://crates.io/thread_local/0.3.6 \
crate://crates.io/toml/0.4.10 \
crate://crates.io/ucd-util/0.1.3 \
crate://crates.io/unicode-width/0.1.5 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/untrusted/0.6.2 \
crate://crates.io/utf8-ranges/1.0.2 \
crate://crates.io/vec_map/0.8.1 \
crate://crates.io/version_check/0.1.5 \
crate://crates.io/void/1.0.2 \
crate://crates.io/walkdir/2.2.7 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-util/0.1.1 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi/0.3.6 \
crate://crates.io/wincolor/0.1.6 \
crate://crates.io/wincolor/1.0.1 \
"

BBCLASSEXTEND = "native"

PACKAGES =+ "${PN}-desktopd ${PN}-realms ${PN}-install ${PN}-image ${PN}-mount"

FILES_${PN}-desktopd = "\
    ${libexecdir}/citadel-desktopd \
    ${datadir}/citadel \
    ${systemd_system_unitdir} \
"

FILES_${PN}-image = "${bindir}/citadel-image"
FILES_${PN}-install = "${libexecdir}/citadel-install"
FILES_${PN}-mount = "${libexecdir}/citadel-mount"
FILES_${PN}-realms = "${bindir}/realms"

TARGET_BIN = "${B}/target/${CARGO_TARGET_SUBDIR}"
DESKTOPD_CONF = "${B}/citadel-desktopd/conf"

do_install() {
    install -d ${D}${bindir}
    install -d ${D}${libexecdir}
    install -d ${D}${datadir}/citadel
    install -d ${D}${systemd_system_unitdir}

    # /usr/libexec/citadel-desktopd
    install -m 755 ${TARGET_BIN}/citadel-desktopd ${D}${libexecdir}
    install -m 644 ${DESKTOPD_CONF}/citadel-desktopd.conf ${D}${datadir}/citadel
    install -m 644 ${DESKTOPD_CONF}/citadel-desktopd.service ${D}${systemd_system_unitdir}

    # /usr/bin/citadel-image
    install -m 755 ${TARGET_BIN}/citadel-image ${D}${bindir}

    # /usr/libexec/citadel-install
    install -m 755 ${TARGET_BIN}/citadel-install ${D}${libexecdir}

    # /usr/libexec/citadel-mount
    install -m 755 ${TARGET_BIN}/citadel-mount ${D}${libexecdir}

    # citadel-realms as /usr/bin/realms
    install -m 755 -T ${TARGET_BIN}/citadel-realms ${D}${bindir}/realms
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
