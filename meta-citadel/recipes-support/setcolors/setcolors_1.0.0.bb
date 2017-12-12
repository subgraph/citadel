# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c9aca5676b62f802a1f83ad9be3359d4"

SRC_URI = "https://github.com/EvanPurkhiser/linux-vt-setcolors/archive/v${PV}.tar.gz \
           file://0001-Fix-CC-in-makefile.patch \
           "
SRC_URI[md5sum] = "789b4dc02d1c4f509b3b01450c7ef980"
SRC_URI[sha256sum] = "ccad9aac5732faab749e8f6f6b40746ce44afec5633374aa77c8e3ac2a8eef42"

S = "${WORKDIR}/linux-vt-${PN}-${PV}"
# NOTE: this is a Makefile-only piece of software, so we cannot generate much of the
# recipe automatically - you will need to examine the Makefile yourself and ensure
# that the appropriate arguments are passed in.

do_configure () {
	# Specify any needed configure commands here
	:
}

EXTRA_OEMAKE = "PREFIX=/usr"
do_compile () {

	# You will almost certainly need to add additional arguments here
	oe_runmake
}

do_install () {
    install -Dm 755 setcolors ${D}/${bindir}/setcolors
    install -d ${D}/${datadir}/setcolors
    cp -R --no-preserve=ownership example-colors/ ${D}/${datadir}/setcolors
}

