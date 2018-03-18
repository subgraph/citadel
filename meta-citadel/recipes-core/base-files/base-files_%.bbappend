
hostname="subgraph"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "\
    file://locale.conf \
    file://environment.sh \
    file://fstab \
    file://99-grsec-debootstrap.conf \
    file://90-citadel-sysctl.conf \
    file://citadel-network.rules \
    file://citadel-ifconfig.sh \
    file://00-storage-tmpfiles.conf \
    file://NetworkManager.conf \
    file://zram-swap.service \
    file://share/dot.vimrc \
"

dirs1777_remove = "${localstatedir}/volatile/tmp"
dirs755="/boot /dev /usr/bin /usr/sbin /usr/lib /etc /etc/default /etc/skel /usr/lib /mnt /proc /home/root /run /usr /usr/bin /usr/share/doc/base-files-3.0.14 /usr/include /usr/lib /usr/sbin /usr/share /usr/share/common-licenses /usr/share/info /usr/share/man /usr/share/misc /var /sys /home /media"

volatiles = ""

inherit systemd
SYSTEMD_SERVICE_${PN} = "zram-swap.service"

# for citadel-ifconfig.sh
RDEPENDS_${PN} = "bash"

do_install_append () {
    install -m 0755 -d ${D}/storage
    install -m 0755 -d ${D}/realms
    install -d ${D}${libdir}/sysctl.d
    install -m 0755 -d ${D}${libexecdir}
    install -m 0755 -d ${D}${sysconfdir}/profile.d
    install -m 0755 -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0755 -d ${D}${sysconfdir}/udev/rules.d
    install -m 0755 -d ${D}${sysconfdir}/NetworkManager
    install -m 0700 -d ${D}${localstatedir}/lib/NetworkManager
    install -m 0700 -d ${D}${localstatedir}/lib/NetworkManager/system-connections

    install -m 0644 ${WORKDIR}/locale.conf ${D}${sysconfdir}/locale.conf
    install -m 0644 ${WORKDIR}/environment.sh ${D}${sysconfdir}/profile.d/environment.sh
    install -m 0644 ${WORKDIR}/fstab ${D}${sysconfdir}/fstab
    install -m 0644 ${WORKDIR}/00-storage-tmpfiles.conf ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${WORKDIR}/NetworkManager.conf ${D}${sysconfdir}/NetworkManager

    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${WORKDIR}/zram-swap.service ${D}${systemd_system_unitdir}

    # disable some pax and grsecurity features so that debootstrap will work
    # this should be removed later
    install -m 0644 ${WORKDIR}/99-grsec-debootstrap.conf ${D}${libdir}/sysctl.d/

    install -m 0644 ${WORKDIR}/90-citadel-sysctl.conf ${D}${libdir}/sysctl.d/

    install -m 0644 ${WORKDIR}/citadel-network.rules ${D}${sysconfdir}/udev/rules.d/
    install -m 0755 ${WORKDIR}/citadel-ifconfig.sh ${D}${libexecdir}

    install -m 0755 ${WORKDIR}/share/dot.vimrc ${D}${sysconfdir}/skel/.vimrc

    ln -s /storage/citadel-state/resolv.conf ${D}${sysconfdir}/resolv.conf
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/etc.conf
    ln -s /dev/null ${D}${sysconfdir}/tmpfiles.d/home.conf
}
