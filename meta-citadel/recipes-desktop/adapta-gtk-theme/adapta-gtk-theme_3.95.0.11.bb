SUMMARY = "An adaptive Gtk+ theme based on Material Design Guidelines"
HOMEPAGE = "https://github.com/adapta-project"

LICENSE = "GPLv2 & CC-BY-SA-4.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://LICENSE_CC_BY_SA4;md5=e277f2eefa979e093628e4fb368f5044"

SRC_URI = "git://github.com/adapta-project/adapta-gtk-theme.git;protocol=https"
SRCREV = "589d00cba24b2076f41f4022140ff442556eb9ca"

S = "${WORKDIR}/git"

DEPENDS = "gdk-pixbuf glib-2.0 glib-2.0-native sassc-native"

inherit pkgconfig autotools-brokensep allarch

FILES_${PN} = "${datadir}/themes"

EXTRA_OECONF = "--disable-cinnamon --disable-flashback --disable-xfce --disable-mate --disable-openbox --disable-plank --disable-telegram --disable-chrome-legacy --disable-gtk_next --disable-parallel"

