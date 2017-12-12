
SUMMARY = "Display variables"
LICENSE = "MIT"


INHIBIT_DEFAULT_DEPS = "1"
PACKAGES = ""

deltask do_fetch
deltask do_unpack
deltask do_patch
deltask do_configure
deltask do_compile
deltask do_install
deltask do_populate_sysroot

inherit showvars nopackages
