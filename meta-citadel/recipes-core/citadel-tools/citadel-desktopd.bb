SUMMARY = "citadel-desktopd"

SRC_URI = "\
crate://crates.io/aho-corasick/0.6.4 \
crate://crates.io/atty/0.2.6 \
crate://crates.io/backtrace-sys/0.1.16 \
crate://crates.io/backtrace/0.3.5 \
crate://crates.io/bitflags/1.0.1 \
crate://crates.io/byteorder/1.2.1 \
crate://crates.io/bytes/0.4.6 \
crate://crates.io/cc/1.0.4 \
crate://crates.io/cfg-if/0.1.2 \
crate://crates.io/chrono/0.4.0 \
crate://crates.io/env_logger/0.5.3 \
crate://crates.io/failure/0.1.1 \
crate://crates.io/failure_derive/0.1.1 \
crate://crates.io/gcc/0.3.54 \
crate://crates.io/inotify-sys/0.1.2 \
crate://crates.io/inotify/0.5.0 \
crate://crates.io/iovec/0.1.2 \
crate://crates.io/lazy_static/1.0.0 \
crate://crates.io/libc/0.2.36 \
crate://crates.io/log/0.4.1 \
crate://crates.io/memchr/2.0.1 \
crate://crates.io/nix/0.10.0 \
crate://crates.io/num-integer/0.1.36 \
crate://crates.io/num-iter/0.1.35 \
crate://crates.io/num-traits/0.1.43 \
crate://crates.io/num-traits/0.2.0 \
crate://crates.io/num/0.1.41 \
crate://crates.io/quote/0.3.15 \
crate://crates.io/redox_syscall/0.1.37 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/regex-syntax/0.4.2 \
crate://crates.io/regex/0.2.5 \
crate://crates.io/rustc-demangle/0.1.5 \
crate://crates.io/serde/1.0.27 \
crate://crates.io/serde_derive/1.0.27 \
crate://crates.io/serde_derive_internals/0.19.0 \
crate://crates.io/syn/0.11.11 \
crate://crates.io/synom/0.11.3 \
crate://crates.io/synstructure/0.6.1 \
crate://crates.io/termcolor/0.3.3 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/thread_local/0.3.5 \
crate://crates.io/time/0.1.39 \
crate://crates.io/toml/0.4.5 \
crate://crates.io/unicode-xid/0.0.4 \
crate://crates.io/unreachable/1.0.0 \
crate://crates.io/utf8-ranges/1.0.0 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi/0.3.4 \
crate://crates.io/wincolor/0.1.5 \
"

inherit cargo

do_install() {
    install -d ${D}${libexecdir}
    install -d ${D}${datadir}/citadel
    install -d ${D}${systemd_system_unitdir}

    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/citadel-desktopd ${D}${libexecdir}
    install -m 644 ${B}/conf/citadel-desktopd.conf ${D}${datadir}/citadel
    install -m 644 ${B}/conf/citadel-desktopd.service ${D}${systemd_system_unitdir}
}

FILES_${PN} += "${datadir}/citadel ${systemd_system_unitdir}"

require citadel-tools.inc
