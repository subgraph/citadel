include citadel-tools.inc

BBCLASSEXTEND = "native"

SRC_URI += " \
crate://crates.io/ansi_term/0.11.0 \
crate://crates.io/atty/0.2.11 \
crate://crates.io/backtrace-sys/0.1.24 \
crate://crates.io/backtrace/0.3.9 \
crate://crates.io/bitflags/1.0.4 \
crate://crates.io/cc/1.0.28 \
crate://crates.io/cfg-if/0.1.6 \
crate://crates.io/clap/2.32.0 \
crate://crates.io/failure/0.1.3 \
crate://crates.io/failure_derive/0.1.3 \
crate://crates.io/lazy_static/1.2.0 \
crate://crates.io/libc/0.2.45 \
crate://crates.io/nix/0.12.0 \
crate://crates.io/proc-macro2/0.4.24 \
crate://crates.io/quote/0.6.10 \
crate://crates.io/redox_syscall/0.1.43 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/ring/0.13.5 \
crate://crates.io/rustc-demangle/0.1.9 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/serde/1.0.82 \
crate://crates.io/serde_derive/1.0.82 \
crate://crates.io/strsim/0.7.0 \
crate://crates.io/syn/0.15.22 \
crate://crates.io/synstructure/0.10.1 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/textwrap/0.10.0 \
crate://crates.io/toml/0.4.10 \
crate://crates.io/unicode-width/0.1.5 \
crate://crates.io/unicode-xid/0.1.0 \
crate://crates.io/untrusted/0.6.2 \
crate://crates.io/vec_map/0.8.1 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.3.6 \
"
