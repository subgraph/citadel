SUMMARY = "SpiderMonkey is Mozilla's JavaScript engine written in C/C++"
HOMEPAGE = "http://www.mozilla.org/js/"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://../../LICENSE;md5=815ca599c9df247a0c7f619bab123dad"

RELEASE_VERSION = "mozjs-52.2.1gnome1"

SRC_URI = "https://download.gnome.org/teams/releng/tarballs-needing-help/mozjs/${RELEASE_VERSION}.tar.gz \
           file://0001-do-not-include-RequiredDefines.patch \
           "
SRC_URI[md5sum] = "72bd9a715ed1ab70b2aebe92969f6b63"
SRC_URI[sha256sum] = "31697943b1dbbb51ba9aee35b8269a353c487d7af4d336010b90054dc4f9b0af"

DEPENDS += "nspr zlib autoconf213-native"

S = "${WORKDIR}/${RELEASE_VERSION}/js/src"

FILES_${PN}-staticdev += "${libdir}/*.ajs"

# prevent FILES_mozjs52-dev from scooping up libmozjs-52.so
FILES_SOLIBSDEV = ""

# put it here instead
PACKAGES =+ "lib${BPN}"
FILES_lib${BPN} = "${libdir}/lib*.so"

inherit autotools pkgconfig perlnative pythonnative

EXTRA_OECONF = " \
    --target=${TARGET_SYS} \
    --host=${BUILD_SYS} \
    --build=${BUILD_SYS} \
    --prefix=${prefix} \
    --libdir=${libdir} \
    --enable-posix-nspr-emulation \
    --with-system-zlib \
    --with-intl-api \
    --disable-jemalloc \
    --disable-static \
    --with-x=no \
"



# native-python uses RPATH to find libpython, but mozilla builder sets
# up a virtualenv which copies the python binary to a new location where
# it can no longer find the expected libpython with embedded RPATH.
# the LD_LIBRARY_PATH setting fixes this problem.

EXTRA_OEMAKE += "SHELL=/bin/sh LD_LIBRARY_PATH=${STAGING_LIBDIR_NATIVE}"

do_configure() {
    export LD_LIBRARY_PATH=${STAGING_LIBDIR_NATIVE}
    export SHELL=/bin/bash
    STAGING_LIBDIR=${STAGING_LIBDIR_NATIVE} 
    STAGING_INCDIR=${STAGING_INCDIR_NATIVE} 
    LD=x86_64-oe-linux-ld
    ${S}/configure ${EXTRA_OECONF} 
}
