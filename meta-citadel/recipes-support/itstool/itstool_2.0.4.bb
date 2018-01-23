SUMMARY = "XML to PO and back again using W3C ITS rules"
HOMEPAGE = "http://itstool.org/"
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
LICENSE = "Unknown & GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=59c57b95fd7d0e9e238ebbc7ad47c5a5 \
                    file://COPYING.GPL3;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "http://files.itstool.org/itstool/itstool-${PV}.tar.bz2"
SRC_URI[md5sum] = "9415ddf6a12012ff688549d2ed767bc5"
SRC_URI[sha256sum] = "97c208b51da33e0b553e830b92655f8deb9132f8fbe9a646771f95c33226eb60"

inherit distutils python3native setuptools3

# WARNING: the following rdepends are determined through basic analysis of the
# python sources, and might not be 100% accurate.
RDEPENDS_${PN} += "python-core"
BBCLASSEXTEND = "native"

# WARNING: We were unable to map the following python package/module
# dependencies to the bitbake packages which include them:
#    __future__
#    gettext
#    hashlib
#    io
#    libxml2
#    optparse
#    os
#    re


