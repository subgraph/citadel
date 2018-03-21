require networkmanager.inc
LIC_FILES_CHKSUM = "file://COPYING;md5=cbbffd568227ada506640fe950a4823b \
                    file://libnm-util/COPYING;md5=1c4fa765d6eb3cd2fbd84344a1b816cd \
                    file://docs/api/html/license.html;md5=77b9e362690c149da196aefe7712db30\
"
EXTRA_OECONF += "--disable-ovs"
SRC_URI[md5sum] = "de3c7147a693da6f80eb22f126086a14"
SRC_URI[sha256sum] = "6af0b1e856a3725f88791f55c4fbb04105dc0b20dbf182aaec8aad16481fac76"


