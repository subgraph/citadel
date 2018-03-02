require gnome-session.inc
SRC_URI[md5sum] = "3f52d4c1d8a64666b126b8fb0a443b28"
SRC_URI[sha256sum] = "09d3495f88750a6bbd64166397c41ac609d2e8e5afec20caec897d526e7438a1"

EXTRA_OEMESON = "-Denable-docbook=false -Denable-man=false"
inherit meson 

