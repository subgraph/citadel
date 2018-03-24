LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=625f055f41728f84a8d7938acc35bdc2"

SRC_URI[archive.md5sum] = "fcbaa7233fc076a6743c22da746956be"
SRC_URI[archive.sha256sum] = "81c528fd1e5e03577acd80fb77798223945f043fd1d4e06920c71202eea90801"

DEPENDS = "glib-2.0 exiv2"

inherit gnome python3native autotools gobject-introspection

EXTRA_OECONF = ""

