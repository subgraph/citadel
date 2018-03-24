LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=2d5025d4aa3495befef8f17206a5b0a1"

SRC_URI[archive.md5sum] = "ece4547298a81105f307369d73c21b9d"
SRC_URI[archive.sha256sum] = "0513aad38e5d3cedd4ae3c551634e3be1b9baaa79775e53b2dba9456f15b01c9"

DEPENDS = "glib-2.0 libsoup-2.4 libxml2"

inherit gnome 

EXTRA_OECONF = "--enable-introspection=no"

