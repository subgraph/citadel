SUMMARY = "XML to PO and back again using W3C ITS rules"
HOMEPAGE = "http://itstool.org/"
LICENSE = "Unknown & GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=59c57b95fd7d0e9e238ebbc7ad47c5a5 \
                    file://COPYING.GPL3;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "http://files.itstool.org/itstool/itstool-${PV}.tar.bz2"
SRC_URI[md5sum] = "9415ddf6a12012ff688549d2ed767bc5"
SRC_URI[sha256sum] = "97c208b51da33e0b553e830b92655f8deb9132f8fbe9a646771f95c33226eb60"

inherit distutils python3native setuptools3

do_configure_prepend() {
        sed -i -e '1s,#!.*,#!${USRBINPATH}/env python3,' ${S}/itstool.in
        sed -i -e '1s,#!.*,#!${USRBINPATH}/env python3,' ${S}/itstool
}
RDEPENDS_${PN} += "python-core"
BBCLASSEXTEND = "native"

