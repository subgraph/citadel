require gjs.inc
LIC_FILES_CHKSUM = "file://COPYING;md5=beb29cf17fabe736f0639b09ee6e76fa \
                    file://COPYING.LGPL;md5=3bf50002aefd002f49e7bb854063f7e7"
SRC_URI[archive.md5sum] = "5f626919a37b75d1b652be6da8723f41"
SRC_URI[archive.sha256sum] = "5524a045e5e1d34a2a510133c662f2685e15ce26ae2ed699fb5d131b6b04a4ca"
FILES_${PN}-dbg += "${datadir}/gjs-1.0/lsan ${datadir}/gjs-1.0/valgrind"
