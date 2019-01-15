SUMMARY = "GNOME file manager"
HOMEPAGE = "https://wiki.gnome.org/action/show/Apps/Files"

LICENSE = "GPLv3 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504 \
                    file://libnautilus-extension/LICENSE;md5=321bf41f280cf805086dd5a720b37785"

GNOMEBASEBUILDCLASS = "meson"
inherit gnomebase gobject-introspection gettext

SRC_URI[archive.md5sum] = "1149a9aa289ddc0db2157c77e0399e1d"
SRC_URI[archive.sha256sum] = "78269bbb0ce538cb9f40dd2feae4fc56dbb7c06651a6b5c9258f6b9631cb4084"


FILES_${PN} += "\
    ${datadir}/glib-2.0/schemas \
    ${datadir}/gnome-shell/search-providers \
    ${datadir}/metainfo/org.gnome.Nautilus.appdata.xml \
    ${datadir}/icons/hicolor/ \
    ${datadir}/dbus-1/services \
"

DEPENDS += "gtk+3 glib-2.0 pango gnome-autoar libxml2 gnome-desktop tracker gexiv2"

EXTRA_OEMESON = "-Dselinux=false -Dpackagekit=false -Dextensions=false -Dintrospection=true"
