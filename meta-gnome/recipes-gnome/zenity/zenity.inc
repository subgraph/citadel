SUMMARY = "GNOME port of Dialog to display dialog boxes from shell scripts"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=3bf50002aefd002f49e7bb854063f7e7"

inherit gnome perlnative gettext 

SRC_URI += "file://0001-Don-t-build-help.patch \
           file://0002-Don-t-include-gdialog.patch \
           "
DEPENDS = "libx11 libnotify glib-2.0 gtk+3 gnome-common autoconf-archive"


EXTRA_OECONF = ""
BBCLASSEXTEND = "native"
