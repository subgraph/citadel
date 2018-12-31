include citadel-tools.inc


SRC_URI += " \
crate://crates.io/autocfg/0.1.1 \
crate://crates.io/backtrace-sys/0.1.26 \
crate://crates.io/backtrace/0.3.13 \
crate://crates.io/bitflags/1.0.4 \
crate://crates.io/cc/1.0.26 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/failure/0.1.3 \
crate://crates.io/failure_derive/0.1.3 \
crate://crates.io/lazy_static/1.2.0 \
crate://crates.io/libc/0.2.45 \
crate://crates.io/nix/0.12.0 \
crate://crates.io/proc-macro2/0.4.24 \
crate://crates.io/quote/0.6.10 \
crate://crates.io/ring/0.13.2 \
crate://crates.io/rustc-demangle/0.1.11 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/serde/1.0.82 \
crate://crates.io/serde_derive/1.0.82 \
crate://crates.io/syn/0.15.23 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/toml/0.4.10 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/untrusted/0.6.2 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.3.6 \
"

inherit cargo

do_install() {
    install -d ${D}${libexecdir}
    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/citadel-mount ${D}${libexecdir}
}
