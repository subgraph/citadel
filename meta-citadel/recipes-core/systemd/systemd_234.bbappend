FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
# https://github.com/systemd/systemd/issues/6375
SRC_URI += "file://0001-dont-process-the-same-method-call-twice-in-logind.patch"

RDEPENDS_${PN}_remove = "systemd-serialgetty"
RDEPENDS_${PN}_remove = "volatile-binds"
RDEPENDS_${PN}_remove = "update-rc.d"

GROUPADD_PARAM_${PN} += "; -r kvm"
PACKAGECONFIG = "\
    efi ldconfig pam usrmerge rfkill backlight binfmt hostnamed ima localed logind machined myhostname \
    nss polkit randomseed seccomp timedated utmp vconsole \
"

do_install_append() {
    rm -f ${D}${sysconfdir}/tmpfiles.d/00-create-volatile.conf
}
