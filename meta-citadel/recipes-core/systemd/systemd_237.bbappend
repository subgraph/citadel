FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

RDEPENDS_${PN}_remove = "systemd-serialgetty"
RDEPENDS_${PN}_remove = "volatile-binds"
RDEPENDS_${PN}_remove = "update-rc.d"

ALTERNATIVE_${PN}_remove = "resolv-conf"

GROUPADD_PARAM_${PN} += "; -r kvm"
PACKAGECONFIG = "\
    efi ldconfig pam usrmerge rfkill backlight binfmt hostnamed ima localed logind machined myhostname \
    nss polkit randomseed seccomp timedated utmp vconsole timesyncd \
"

do_install_append() {
    rm -f ${D}${sysconfdir}/tmpfiles.d/00-create-volatile.conf
    echo "a4e415feff81466c925aab34b0c35a3c" > ${D}${sysconfdir}/machine-id
}
