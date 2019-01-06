DESCRIPTION = ""
HOMEPAGE = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SECTION = ""
DEPENDS = ""

S = "${WORKDIR}"

DEFAULT_REALM_UNITS = "\
    file://systemd/launch-default-realm.path \
    file://systemd/launch-default-realm.service \
    file://systemd/watch-run-user.path \
    file://systemd/watch-run-user.service \
"

MODPROBE_CONFIG = "\
    file://modprobe.d/audio_powersave.conf \
"

SYSCTL_CONFIG = "\
    file://sysctl/99-grsec-debootstrap.conf \
    file://sysctl/90-citadel-sysctl.conf \
"

UDEV_RULES = "\
    file://udev/citadel-network.rules \
    file://udev/pci-pm.rules \
    file://udev/scsi-alpm.rules \
"

IPTABLES_RULES = "\
    file://iptables/empty-filter.rules \
    file://iptables/iptables.rules \
"

SRC_URI = "\
    file://locale.conf \
    file://environment.sh \
    file://fstab \
    file://citadel-ifconfig.sh \
    file://00-storage-tmpfiles.conf \
    file://NetworkManager.conf \
    file://share/dot.bashrc \
    file://share/dot.profile \
    file://share/dot.vimrc \
    file://polkit/citadel.rules \
    file://iptables-flush.sh \
    file://systemd/zram-swap.service \
    file://systemd/iptables.service \
    file://citadel/citadel-image.conf \
    ${DEFAULT_REALM_UNITS} \
    ${MODPROBE_CONFIG} \
    ${SYSCTL_CONFIG} \
    ${UDEV_RULES} \
    ${IPTABLES_RULES} \
"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "-m -u 1000 -s /bin/bash citadel"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

# for citadel-ifconfig.sh
RDEPENDS_${PN} = "bash"

inherit allarch systemd useradd

SYSTEMD_SERVICE_${PN} = "zram-swap.service watch-run-user.path iptables.service"

do_install() {
    install -m 0755 -d ${D}/storage
    install -m 0755 -d ${D}/realms
    install -d ${D}${libdir}/sysctl.d
    install -m 0755 -d ${D}${libexecdir}
    install -m 0755 -d ${D}${sysconfdir}/profile.d
    install -m 0755 -d ${D}${sysconfdir}/skel
    install -m 0755 -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0755 -d ${D}${sysconfdir}/udev/rules.d
    install -m 0755 -d ${D}${sysconfdir}/NetworkManager
    install -m 0755 -d ${D}${sysconfdir}/polkit-1/rules.d
    install -m 0755 -d ${D}${sysconfdir}/modprobe.d
    install -m 0755 -d ${D}${datadir}/citadel
    install -m 0755 -d ${D}${datadir}/iptables
    install -m 0700 -d ${D}${localstatedir}/lib/NetworkManager
    install -m 0700 -d ${D}${localstatedir}/lib/NetworkManager/system-connections

    install -m 0644 ${WORKDIR}/locale.conf ${D}${sysconfdir}/locale.conf
    install -m 0644 ${WORKDIR}/environment.sh ${D}${sysconfdir}/profile.d/environment.sh
    install -m 0644 ${WORKDIR}/fstab ${D}${sysconfdir}/fstab
    install -m 0644 ${WORKDIR}/00-storage-tmpfiles.conf ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${WORKDIR}/NetworkManager.conf ${D}${sysconfdir}/NetworkManager

    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/systemd/zram-swap.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/systemd/iptables.service ${D}${systemd_system_unitdir}

    install -m 644 ${WORKDIR}/systemd/watch-run-user.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/systemd/watch-run-user.service ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/systemd/launch-default-realm.path ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/systemd/launch-default-realm.service ${D}${systemd_system_unitdir}

    # disable some pax and grsecurity features so that debootstrap will work
    # this should be removed later
    install -m 0644 ${WORKDIR}/sysctl/99-grsec-debootstrap.conf ${D}${libdir}/sysctl.d/

    install -m 0644 ${WORKDIR}/sysctl/90-citadel-sysctl.conf ${D}${libdir}/sysctl.d/

    install -m 0644 ${WORKDIR}/udev/citadel-network.rules ${D}${sysconfdir}/udev/rules.d/
    install -m 0755 ${WORKDIR}/citadel-ifconfig.sh ${D}${libexecdir}

    install -m 0644 ${WORKDIR}/udev/pci-pm.rules ${D}${sysconfdir}/udev/rules.d/
    install -m 0644 ${WORKDIR}/udev/scsi-alpm.rules ${D}${sysconfdir}/udev/rules.d/

    install -m 0644 ${WORKDIR}/iptables/iptables.rules ${D}${datadir}/iptables/
    install -m 0644 ${WORKDIR}/iptables/empty-filter.rules ${D}${datadir}/iptables/
    install -m 0644 ${WORKDIR}/iptables-flush.sh ${D}${datadir}/iptables/

    install -m 0644 ${WORKDIR}/share/dot.bashrc ${D}${sysconfdir}/skel/.bashrc
    install -m 0644 ${WORKDIR}/share/dot.profile ${D}${sysconfdir}/skel/.profile
    install -m 0644 ${WORKDIR}/share/dot.vimrc ${D}${sysconfdir}/skel/.vimrc

    install -m 0644 ${WORKDIR}/polkit/citadel.rules ${D}${sysconfdir}/polkit-1/rules.d/

    install -m 0644 ${WORKDIR}/modprobe.d/audio_powersave.conf ${D}${sysconfdir}/modprobe.d/

    install -m 0644 ${S}/citadel/citadel-image.conf ${D}${datadir}/citadel

    # This probably belongs in lvm2 recipe
    install -d ${D}${systemd_system_unitdir}/sysinit.target.wants
    ln -s ../lvm2-lvmetad.socket ${D}${systemd_system_unitdir}/sysinit.target.wants/lvm2-lvmetad.socket

    ln -s /storage/citadel-state/resolv.conf ${D}${sysconfdir}/resolv.conf
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/etc.conf
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/home.conf

    install -d ${D}${datadir}/themes
    install -d ${D}${datadir}/icons
    install -d ${D}${libdir}/modules
    install -d ${D}${libdir}/firmware
    install -d ${D}${datadir}/backgrounds
    install -d ${D}/opt/share
}

FILES_${PN} = "/"
