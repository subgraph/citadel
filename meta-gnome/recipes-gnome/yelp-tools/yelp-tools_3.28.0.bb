LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d67c6f9f1515506abfea4f0d920c0774 \
                    file://COPYING.GPL;md5=eb723b61539feef013de476e68b5c50a \
                    file://xslt/mal-license.xsl;md5=b02305a5e9d23df0f83b0c61a8883509"

SRC_URI[archive.md5sum] = "76907906611daae8f19a5276dd65f55d"
SRC_URI[archive.sha256sum] = "82dbfeea2359dfef8ee92c7580c7f03768d12f9bf67d839f03a5e9b0686dc1ac"

DEPENDS = "yelp-xsl xmlto-native libxml2-native itstool-native"

inherit gnomebase pkgconfig autotools

EXTRA_OECONF = ""

BBCLASSEXTEND = "native"
