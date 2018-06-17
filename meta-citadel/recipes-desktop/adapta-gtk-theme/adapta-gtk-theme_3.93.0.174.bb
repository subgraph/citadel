SUMMARY = "An adaptive Gtk+ theme based on Material Design Guidelines"
HOMEPAGE = "https://github.com/adapta-project"

LICENSE = "GPLv2 & CC-BY-SA-4.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
                    file://LICENSE_CC_BY_SA4;md5=e277f2eefa979e093628e4fb368f5044"

SRC_URI = "https://github.com/adapta-project/adapta-gtk-theme/archive/${PV}.tar.gz;downloadfilename=adapta-gtk-theme-${PV}.tar.gz"
SRC_URI[md5sum] = "a76029d793403169bd505a1db45c891c"
SRC_URI[sha256sum] = "5bbd84e140b2a7271b29078f1d5bb3246248c7d71926e7d71582fdfa24858fa1"


S = "${WORKDIR}/${BPN}-${PV}"

DEPENDS = "gdk-pixbuf glib-2.0 glib-2.0-native sassc-native"

inherit pkgconfig autotools-brokensep allarch

FILES_${PN} = "${datadir}/themes"

EXTRA_OECONF = "--disable-cinnamon --disable-flashback --disable-xfce --disable-mate --disable-openbox --disable-plank --disable-telegram --disable-chrome_legacy --disable-gtk_next --disable-gtk_legacy --disable-parallel"

