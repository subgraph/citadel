# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   COPYING
#   xslt/mal-license.xsl
#
# NOTE: multiple licenses have been detected; they have been separated with &
# in the LICENSE value for now since it is a reasonable assumption that all
# of the licenses apply. If instead there is a choice between the multiple
# licenses then you should change the value to separate the licenses with |
# instead of &. If there is any doubt, check the accompanying documentation
# to determine which situation is applicable.
LICENSE = "Unknown & GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d67c6f9f1515506abfea4f0d920c0774 \
                    file://COPYING.GPL;md5=eb723b61539feef013de476e68b5c50a \
                    file://xslt/mal-license.xsl;md5=b02305a5e9d23df0f83b0c61a8883509"

SRC_URI = "https://download.gnome.org/sources/yelp-tools/3.18/yelp-tools-${PV}.tar.xz"
SRC_URI[md5sum] = "ceca436ff2ab3900dde718c0f0286f3f"
SRC_URI[sha256sum] = "c6c1d65f802397267cdc47aafd5398c4b60766e0a7ad2190426af6c0d0716932"

# NOTE: the following prog dependencies are unknown, ignoring: xsltproc xmllint itstool
DEPENDS = "yelp-xsl xmlto-native libxml2-native itstool-native"

# NOTE: if this software is not capable of being built in a separate build directory
# from the source, you should replace autotools with autotools-brokensep in the
# inherit line
inherit pkgconfig autotools

# Specify any options you want to pass to the configure script using EXTRA_OECONF:
EXTRA_OECONF = ""

BBCLASSEXTEND = "native"
