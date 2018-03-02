# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   COPYING
#
# NOTE: multiple licenses have been detected; they have been separated with &
# in the LICENSE value for now since it is a reasonable assumption that all
# of the licenses apply. If instead there is a choice between the multiple
# licenses then you should change the value to separate the licenses with |
# instead of &. If there is any doubt, check the accompanying documentation
# to determine which situation is applicable.
LICENSE = "Unknown & GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=3e2bad3c5e3990988f9fa1bc5785b147 \
                    file://COPYING.GPL;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LGPL;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI = "http://ftp.gnome.org/pub/gnome/sources/yelp-xsl/3.20/yelp-xsl-${PV}.tar.xz"
SRC_URI[md5sum] = "2332716e6e39125a942bc761a6f94211"
SRC_URI[sha256sum] = "dc61849e5dca473573d32e28c6c4e3cf9c1b6afe241f8c26e29539c415f97ba0"

# NOTE: the following prog dependencies are unknown, ignoring: xmllint xsltproc itstool
DEPENDS = "intltool-native itstool-native"

# NOTE: if this software is not capable of being built in a separate build directory
# from the source, you should replace autotools with autotools-brokensep in the
# inherit line
inherit gettext autotools

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = ""
BBCLASSEXTEND = "native"

