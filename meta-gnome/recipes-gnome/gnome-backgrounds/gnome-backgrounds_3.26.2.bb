SUMMARY = "Default GNOME desktop background images"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=75859989545e37968a99b631ef42722e"

SRC_URI[archive.md5sum] = "479f8ff4460b2bb0fc17a193e5818a7f"
SRC_URI[archive.sha256sum] = "3a8ba8d3463d70bce2377b168218e32367c0020f2d0caf611e7e39066081f94f"

FILES_${PN} += "\
    ${datadir}/backgrounds/gnome \
    ${datadir}/gnome-background-properties \
"

inherit gnomebase gettext

EXTRA_OECONF = ""

