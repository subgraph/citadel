require gnome-session.inc
SRC_URI[archive.md5sum] = "4c108adbf6ebe25486d41a9bc8cc340c"
SRC_URI[archive.sha256sum] = "d9414b368db982d3837ca106e64019f18e6cdd5b13965bea6c7d02ddf5103708"

DEPENDS += "gnome-common-native"

EXTRA_OECONF = "--enable-man=no"
inherit pkgconfig gettext autotools

