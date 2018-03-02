
# ugh....
# https://bugzilla.mozilla.org/show_bug.cgi?id=104642

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=361b6b837cad26c6900a926b62aada5f"

SRC_URI = "http://ftp.gnu.org/gnu/autoconf/autoconf-2.13.tar.gz"
SRC_URI[md5sum] = "9de56d4a161a723228220b0f425dc711"
SRC_URI[sha256sum] = "f0611136bee505811e9ca11ca7ac188ef5323a8e2ef19cffd3edb3cf08fd791e"

DEPENDS = "m4-native gnu-config-native"
RDEPENDS_${PN} = "perl"
export PERL = "${USRBINPATH}/perl"
S = "${WORKDIR}/autoconf-2.13"
FILES_${PN} += "/usr/share/autoconf"

inherit autotools showvars

do_configure() {
    oe_runconf --program-suffix=213 --infodir=${D}/${infodir} --bindir=${D}/${bindir} --datadir=${D}/${datadir}
}

EXTRA_OECONF = "--program-suffix=213"
BBCLASSEXTEND = "native"

