SUMMARY = "GNOME port of Dialog to display dialog boxes from shell scripts"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=3bf50002aefd002f49e7bb854063f7e7"

inherit gnome perlnative gettext 

SRC_URI[archive.md5sum] = "b2180f4ef4fcb2ee90da8c65bd5241af"
SRC_URI[archive.sha256sum] = "995ef696616492c40be6da99919851d41faed6643a97c9d24743b46bc8b537f2"

SRC_URI += "file://0001-Don-t-build-help.patch \
           file://0002-Don-t-include-gdialog.patch \
           "
DEPENDS = "libx11 libnotify glib-2.0 gtk+3 gnome-common autoconf-archive"

EXTRA_OECONF = ""
BBCLASSEXTEND = "native"
