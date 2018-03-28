hostname="subgraph"

dirs1777_remove = "${localstatedir}/volatile/tmp"

dirs755="/boot /dev /usr/bin /usr/sbin /usr/lib /etc /etc/default /etc/skel /usr/lib /mnt /proc /home/root /run /usr /usr/bin /usr/share/doc/base-files-3.0.14 /usr/include /usr/lib /usr/sbin /usr/share /usr/share/common-licenses /usr/share/info /usr/share/man /usr/share/misc /var /sys /home /media"

volatiles = ""

do_install_append () {
    rm ${D}${sysconfdir}/fstab
    rm ${D}${sysconfdir}/skel/.bashrc
    rm ${D}${sysconfdir}/skel/.profile

}
