require gnome-session.inc
SRC_URI[archive.md5sum] = "ceed281645d1f98768de957dc8e79ee6"
SRC_URI[archive.sha256sum] = "2e935ae2cacca2e1a7bff22bbe799797c74f79a33261093ceb3fd514b39bd14d"

# https://bugzilla.gnome.org/show_bug.cgi?id=794757
SRC_URI += "file://dont-check-for-have-xtrans.patch"

EXTRA_OEMESON = "-Denable-docbook=false -Denable-man=false"
inherit meson 

