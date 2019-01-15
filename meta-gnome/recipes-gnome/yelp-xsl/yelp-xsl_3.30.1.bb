LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=3e2bad3c5e3990988f9fa1bc5785b147 \
                    file://COPYING.GPL;md5=eb723b61539feef013de476e68b5c50a \
                    file://COPYING.LGPL;md5=a6f89e2100d9b6cdffcea4f398e37343"

SRC_URI[archive.md5sum] = "371f7379f1614e602f861922182bbfbe"
SRC_URI[archive.sha256sum] = "fcef31c5938c6654976bbabb8b5d0d9e49fa2ce79136db74ca213056fdb8cf39"

DEPENDS = "intltool-native itstool-native"

inherit gnomebase gettext 

EXTRA_OECONF = ""
BBCLASSEXTEND = "native"

