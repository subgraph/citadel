SUMMARY = "citadel-rootfs"

SRC_URI = "\
crate://crates.io/ansi_term/0.10.2 \
crate://crates.io/arrayref/0.3.4 \
crate://crates.io/atty/0.2.6 \
crate://crates.io/backtrace-sys/0.1.16 \
crate://crates.io/backtrace/0.3.5 \
crate://crates.io/bitflags/1.0.1 \
crate://crates.io/block-buffer/0.3.3 \
crate://crates.io/build_const/0.2.0 \
crate://crates.io/byte-tools/0.2.0 \
crate://crates.io/byteorder/1.2.1 \
crate://crates.io/bytes/0.4.6 \
crate://crates.io/cc/1.0.4 \
crate://crates.io/cfg-if/0.1.2 \
crate://crates.io/clap/2.30.0 \
crate://crates.io/clear_on_drop/0.2.3 \
crate://crates.io/crc/1.7.0 \
crate://crates.io/curve25519-dalek/0.14.4 \
crate://crates.io/digest/0.7.2 \
crate://crates.io/ed25519-dalek/0.6.1 \
crate://crates.io/failure/0.1.1 \
crate://crates.io/failure_derive/0.1.1 \
crate://crates.io/fake-simd/0.1.2 \
crate://crates.io/fuchsia-zircon-sys/0.3.3 \
crate://crates.io/fuchsia-zircon/0.3.3 \
crate://crates.io/gcc/0.3.54 \
crate://crates.io/generic-array/0.9.0 \
crate://crates.io/iovec/0.1.2 \
crate://crates.io/libc/0.2.36 \
crate://crates.io/log/0.4.1 \
crate://crates.io/lzma-rs/0.1.0 \
crate://crates.io/nix/0.10.0 \
crate://crates.io/num-traits/0.1.43 \
crate://crates.io/num-traits/0.2.0 \
crate://crates.io/quote/0.3.15 \
crate://crates.io/rand/0.4.2 \
crate://crates.io/redox_syscall/0.1.37 \
crate://crates.io/redox_termios/0.1.1 \
crate://crates.io/rustc-demangle/0.1.6 \
crate://crates.io/rustc-serialize/0.3.24 \
crate://crates.io/serde/1.0.27 \
crate://crates.io/serde_derive/1.0.27 \
crate://crates.io/serde_derive_internals/0.19.0 \
crate://crates.io/sha2/0.7.0 \
crate://crates.io/strsim/0.7.0 \
crate://crates.io/subtle/0.3.0 \
crate://crates.io/subtle/0.5.1 \
crate://crates.io/syn/0.11.11 \
crate://crates.io/synom/0.11.3 \
crate://crates.io/synstructure/0.6.1 \
crate://crates.io/termion/1.5.1 \
crate://crates.io/textwrap/0.9.0 \
crate://crates.io/toml/0.4.5 \
crate://crates.io/typenum/1.9.0 \
crate://crates.io/unicode-width/0.1.4 \
crate://crates.io/unicode-xid/0.0.4 \
crate://crates.io/vec_map/0.8.0 \
crate://crates.io/void/1.0.2 \
crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
crate://crates.io/winapi/0.2.8 \
crate://crates.io/winapi/0.3.4 \
"

do_install() {
    install -d ${D}${bindir}
    install -d ${D}${datadir}/citadel

    install -m 755 ${B}/target/${CARGO_TARGET_SUBDIR}/citadel-rootfs ${D}${bindir}
    install -m 644 ${B}/conf/citadel-rootfs.conf ${D}${datadir}/citadel
}

inherit cargo
require citadel-tools.inc
