include citadel-tools.inc

SRC_URI += " \
crate://crates.io/autocfg/0.1.1 \
crate://crates.io/backtrace-sys/0.1.28 \
crate://crates.io/backtrace/0.3.13 \
crate://crates.io/cc/1.0.28 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/failure/0.1.3 \
crate://crates.io/failure_derive/0.1.3 \
crate://crates.io/kernel32-sys/0.2.2 \
crate://crates.io/libc/0.2.45 \
crate://crates.io/proc-macro2/0.4.24 \
crate://crates.io/quote/0.6.10 \
crate://crates.io/rpassword/2.1.0 \
crate://crates.io/rustc-demangle/0.1.11 \
crate://crates.io/syn/0.15.23 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/winapi-build/0.1.1 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi/0.3.6 \
"

inherit cargo

do_install() {
    install -d ${D}${libexecdir}
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/citadel-install ${D}${libexecdir}
}

