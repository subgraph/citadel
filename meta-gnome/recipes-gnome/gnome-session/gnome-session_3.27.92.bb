require gnome-session.inc
SRC_URI[archive.md5sum] = "ceed281645d1f98768de957dc8e79ee6"
SRC_URI[archive.sha256sum] = "2e935ae2cacca2e1a7bff22bbe799797c74f79a33261093ceb3fd514b39bd14d"

EXTRA_OEMESON = "-Denable-docbook=false -Denable-man=false"
inherit meson 

