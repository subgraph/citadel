SUMMARY = "Customize advanced GNOME desktop configuration options"
HOMEPAGE = "https://wiki.gnome.org/action/show/Apps/Tweaks"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSES/GPL-3.0;md5=9eef91148a9b14ec7f9df333daebc746"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gettext

SRC_URI[archive.md5sum] = "5c4f9181cf18ce229f63d84723943721"
SRC_URI[archive.sha256sum] = "3f78551515eadf848c9140482e08842a5552acdc2987d8cc13f496187cd4c348"

DEPENDS =  "glib-2.0 glib-2.0-native"

RDEPENDS_${PN} = "python3 python3-pygobject"

FILES_${PN} += "\
    ${libdir}/python3.5/site-packages/gtweak \
    ${datadir}/icons/hicolor \
    ${datadir}/metainfo \
"

