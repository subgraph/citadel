
SUMMARY = "systemd configured for use in initramfs"
HOMEPAGE = "http://www.freedesktop.org/wiki/Software/systemd"

LICENSE = "GPLv2 & LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE.GPL2;md5=751419260aa954499f7abaabaa882bbe \
                    file://LICENSE.LGPL2.1;md5=4fbd65380cdd255951079008b364516c"

DEPENDS = "intltool-native gperf-native libcap libcgroup util-linux kmod cryptsetup"
inherit useradd pkgconfig autotools 
SRCREV = "c1edab7ad1e7ccc9be693bedfd464cd1cbffb395"

SRC_URI = "git://github.com/systemd/systemd.git;protocol=git"

S = "${WORKDIR}/git"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "-r lock; -r systemd-journal; -r kvm"
# Hardcode target binary paths to avoid AC_PROG_PATH in the systemd
# configure script detecting and setting paths from sysroot or host.
CACHED_CONFIGUREVARS_class-target = " \
    ac_cv_path_KEXEC=${sbindir}/kexec \
    ac_cv_path_KILL=${base_bindir}/kill \
    ac_cv_path_KMOD=${base_bindir}/kmod \
    ac_cv_path_MOUNT_PATH=${base_bindir}/mount \
    ac_cv_path_QUOTACHECK=${sbindir}/quotacheck \
    ac_cv_path_QUOTAON=${sbindir}/quotaon \
    ac_cv_path_SULOGIN=${base_sbindir}/sulogin \
    ac_cv_path_UMOUNT_PATH=${base_bindir}/umount \
"

# Helper variables to clarify locations.  This mirrors the logic in systemd's
# build system.
rootprefix ?= "${root_prefix}"
rootlibdir ?= "${base_libdir}"
systemd_unitdir = "${rootprefix}/lib"

EXTRA_OECONF = " \
    --without-python \
    --with-rootlibdir=${rootlibdir} \
    --with-rootprefix=${rootprefix} \
    --with-sysvrcnd-path=${sysconfdir} \
    --disable-audit \
    --disable-binfmt \
    --disable-bzip2 \
    --disable-coredump \
    --disable-dbus \
    --disable-efi \
    --disable-elfutils \
    --disable-firstboot \
    --disable-gcrypt \
    --disable-hibernate \
    --disable-ima \
    --disable-importd \
    --disable-libiptc \
    --disable-libcurl \
    --disable-libidn \
    --disable-localed \
    --disable-lz4 \
    --disable-machined \
    --disable-manpages \
    --disable-microhttpd \
    --disable-myhostname \
    --disable-networkd \
    --disable-nss-systemd \
    --disable-pam \
    --disable-polkit \
    --disable-qrencode \
    --disable-quotacheck \
    --disable-randomseed \
    --disable-resolved \
    --disable-rfkill \
    --disable-seccomp \
    --disable-selinux \
    --disable-smack \
    --disable-sysusers \
    --disable-timedated \
    --disable-timesyncd \
    --disable-utmp \
    --disable-xkbcommon \
    --disable-xz \
    --disable-zlib \
    \
    --enable-backlight \
    --enable-libcryptsetup \
    --enable-split-usr \
    --enable-vconsole \
    --enable-logind \
"

COMPILER_NM ?= "${HOST_PREFIX}gcc-nm"
COMPILER_AR ?= "${HOST_PREFIX}gcc-ar"
COMPILER_RANLIB ?= "${HOST_PREFIX}gcc-ranlib"

do_configure_prepend() {
	export NM="${COMPILER_NM}"
	export AR="${COMPILER_AR}"
	export RANLIB="${COMPILER_RANLIB}"
	export KMOD="${base_bindir}/kmod"
	if [ -d ${S}/units.pre_sed ] ; then
		cp -r ${S}/units.pre_sed ${S}/units
	else
		cp -r ${S}/units ${S}/units.pre_sed
	fi
	sed -i -e 's:-DTEST_DIR=\\\".*\\\":-DTEST_DIR=\\\"${PTEST_PATH}/tests/test\\\":' ${S}/Makefile.am
	sed -i -e 's:-DCATALOG_DIR=\\\".*\\\":-DCATALOG_DIR=\\\"${PTEST_PATH}/tests/catalog\\\":' ${S}/Makefile.am
}

do_install() {
	autotools_do_install
	# Provide support for initramfs
        install -d ${D}/sysroot
	[ ! -e ${D}/init ] && ln -s ${systemd_unitdir}/systemd/systemd ${D}/init
	#[ ! -e ${D}/${base_sbindir}/udevd ] && ln -s ${systemd_unitdir}/systemd/systemd-udevd ${D}/${base_sbindir}/udevd

	# Create machine-id
	# 20:12 < mezcalero> koen: you have three options: a) run systemd-machine-id-setup at install time, b) have / read-only and an empty file there (for stateless) and c) boot with / writable
	touch ${D}${sysconfdir}/machine-id

	install -d ${D}${sysconfdir}/udev/rules.d/
	install -d ${D}${sysconfdir}/tmpfiles.d
	#install -m 0644 ${WORKDIR}/*.rules ${D}${sysconfdir}/udev/rules.d/
	#install -d ${D}${libdir}/pkgconfig
	#install -m 0644 ${B}/src/udev/udev.pc ${D}${libdir}/pkgconfig/

	#install -m 0644 ${WORKDIR}/00-create-volatile.conf ${D}${sysconfdir}/tmpfiles.d/

	#chown root:systemd-journal ${D}/${localstatedir}/log/journal

	# Delete journal README, as log can be symlinked inside volatile.
	rm -f ${D}/${localstatedir}/log/README

	# Set the maximium size of runtime journal to 64M as default
	sed -i -e 's/.*RuntimeMaxUse.*/RuntimeMaxUse=64M/' ${D}${sysconfdir}/systemd/journald.conf

	# this file is needed to exist if networkd is disabled but timesyncd is still in use since timesyncd checks it
	# for existence else it fails
	if [ -s ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf ]; then
		${@bb.utils.contains('PACKAGECONFIG', 'networkd', ':', 'sed -i -e "\$ad /run/systemd/netif/links 0755 root root -" ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf', d)}
	fi
	if ! ${@bb.utils.contains('PACKAGECONFIG', 'resolved', 'true', 'false', d)}; then
		echo 'L! ${sysconfdir}/resolv.conf - - - - ../run/systemd/resolve/resolv.conf' >>${D}${exec_prefix}/lib/tmpfiles.d/etc.conf
		echo 'd /run/systemd/resolve 0755 root root -' >>${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
		echo 'f /run/systemd/resolve/resolv.conf 0644 root root' >>${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
		ln -s ../run/systemd/resolve/resolv.conf ${D}${sysconfdir}/resolv-conf.systemd
	else
		sed -i -e "s%^L! /etc/resolv.conf.*$%L! /etc/resolv.conf - - - - ../run/systemd/resolve/resolv.conf%g" ${D}${exec_prefix}/lib/tmpfiles.d/etc.conf
		ln -s ../run/systemd/resolve/resolv.conf ${D}${sysconfdir}/resolv-conf.systemd
	fi
	install -Dm 0755 ${S}/src/systemctl/systemd-sysv-install.SKELETON ${D}${systemd_system_unitdir}/systemd-sysv-install

	# If polkit is setup fixup permissions and ownership
	if ${@bb.utils.contains('PACKAGECONFIG', 'polkit', 'true', 'false', d)}; then
		if [ -d ${D}${datadir}/polkit-1/rules.d ]; then
			chmod 700 ${D}${datadir}/polkit-1/rules.d
			chown polkitd:root ${D}${datadir}/polkit-1/rules.d
		fi
	fi
}

PACKAGES += "${PN}-analyze ${PN}-not-used"

FILES_${PN}-analyze = "${bindir}/systemd-analyze"
RDEPENDS_${PN}-analyze = "${PN}"

RDEPENDS_${PN}-not-used = "bash"
FILES_${PN}-not-used = "\
    ${sysconfdir} \
    ${datadir}/zsh \
    ${datadir}/dbus-1 \
    ${datadir}/bash-completion \
    ${datadir}/factory \
    ${libdir}/udev/hwdb.d \
    ${libdir}/udev/collect \
    ${libdir}/udev/v4l_id \
    ${libdir}/udev/mtd_probe \
    ${libdir}/rpm \
    ${libdir}/environment.d \
    ${libdir}/kernel/install.d \
    ${libdir}/modules-load.d \
    ${libdir}/tmpfiles.d \
    ${bindir}/systemd-* \
    ${bindir}/busctl \
    ${bindir}/hostnamectl \
    ${bindir}/kernel-install \
"

INSANE_SKIP_${PN} += "dev-so"
PRIVATE_LIBS = "libsystemd-shared-${PV}.so libsystemd.so.0 libudev.so.1"

FILES_${PN} = "\
    ${libdir}/libudev* \
    ${libdir}/libsystemd* \
    ${systemd_unitdir}/systemd/libsystemd-shared* \
    \
    ${systemd_unitdir}/systemd/systemd \
    ${systemd_unitdir}/systemd/systemd-cgroups-agent \
    ${systemd_unitdir}/systemd/systemd-cryptsetup \
    ${systemd_unitdir}/systemd/systemd-shutdown \
    ${systemd_unitdir}/systemd/systemd-reply-password \
    ${systemd_unitdir}/systemd/systemd-fsck \
    ${systemd_unitdir}/systemd/systemd-udevd \
    ${systemd_unitdir}/systemd/systemd-journald \
    ${systemd_unitdir}/systemd/systemd-sysctl \
    ${systemd_unitdir}/systemd/systemd-modules-load \
    ${systemd_unitdir}/systemd/systemd-vconsole-setup \
    ${systemd_unitdir}/systemd/system-generators/systemd-fstab-generator \
    ${systemd_unitdir}/systemd/system-generators/systemd-gpt-auto-generator \
    ${systemd_unitdir}/systemd/system-generators/systemd-cryptsetup-generator \
    \
    ${systemd_system_unitdir}/cryptsetup-pre.target \
    ${systemd_system_unitdir}/cryptsetup.target \
    ${systemd_system_unitdir}/emergency.target \
    ${systemd_system_unitdir}/emergency.service \
    ${systemd_system_unitdir}/sysinit.target \
    ${systemd_system_unitdir}/basic.target \
    ${systemd_system_unitdir}/halt.target \
    ${systemd_system_unitdir}/kexec.target \
    ${systemd_system_unitdir}/local-fs.target \
    ${systemd_system_unitdir}/local-fs-pre.target \
    ${systemd_system_unitdir}/remote-fs.target \
    ${systemd_system_unitdir}/remote-fs-pre.target \
    ${systemd_system_unitdir}/multi-user.target \
    ${systemd_system_unitdir}/network.target \
    ${systemd_system_unitdir}/network-pre.target \
    ${systemd_system_unitdir}/network-online.target \
    ${systemd_system_unitdir}/nss-lookup.target \
    ${systemd_system_unitdir}/nss-user-lookup.target \
    ${systemd_system_unitdir}/poweroff.target \
    ${systemd_system_unitdir}/reboot.target \
    ${systemd_system_unitdir}/rescue.target \
    ${systemd_system_unitdir}/rpcbind.target \
    ${systemd_system_unitdir}/shutdown.target \
    ${systemd_system_unitdir}/final.target \
    ${systemd_system_unitdir}/sigpwr.target \
    ${systemd_system_unitdir}/sockets.target \
    ${systemd_system_unitdir}/swap.target \
    ${systemd_system_unitdir}/timers.target \
    ${systemd_system_unitdir}/paths.target \
    ${systemd_system_unitdir}/umount.target \
    \
    ${systemd_system_unitdir}/sys-kernel-config.mount \
    \
    ${systemd_system_unitdir}/kmod-static-nodes.service \
    ${systemd_system_unitdir}/systemd-tmpfiles-setup.service \
    ${systemd_system_unitdir}/systemd-tmpfiles-setup-dev.service \
    ${systemd_system_unitdir}/systemd-ask-password-console.path \
    ${systemd_system_unitdir}/systemd-udevd-control.socket \
    ${systemd_system_unitdir}/systemd-udevd-kernel.socket \
    ${systemd_system_unitdir}/systemd-ask-password-plymouth.path \
    ${systemd_system_unitdir}/systemd-journald.socket \
    ${systemd_system_unitdir}/systemd-journald-audit.socket \
    ${systemd_system_unitdir}/systemd-ask-password-console.service \
    ${systemd_system_unitdir}/systemd-modules-load.service \
    ${systemd_system_unitdir}/systemd-halt.service \
    ${systemd_system_unitdir}/systemd-poweroff.service \
    ${systemd_system_unitdir}/systemd-reboot.service \
    ${systemd_system_unitdir}/systemd-kexec.service \
    ${systemd_system_unitdir}/systemd-fsck@.service \
    ${systemd_system_unitdir}/systemd-udevd.service \
    ${systemd_system_unitdir}/systemd-udev-trigger.service \
    ${systemd_system_unitdir}/systemd-udev-settle.service \
    ${systemd_system_unitdir}/systemd-ask-password-plymouth.service \
    ${systemd_system_unitdir}/systemd-journald.service \
    ${systemd_system_unitdir}/systemd-vconsole-setup.service \
    ${systemd_system_unitdir}/systemd-random-seed-load.service \
    ${systemd_system_unitdir}/systemd-random-seed.service \
    ${systemd_system_unitdir}/systemd-sysctl.service \
    \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-modules-load.service \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-ask-password-console.path \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-journald.service \
    ${systemd_system_unitdir}/sockets.target.wants/systemd-udevd-control.socket \
    ${systemd_system_unitdir}/sockets.target.wants/systemd-udevd-kernel.socket \
    ${systemd_system_unitdir}/sockets.target.wants/systemd-journald.socket \
    ${systemd_system_unitdir}/sockets.target.wants/systemd-journald-audit.socket \
    ${systemd_system_unitdir}/sockets.target.wants/systemd-journald-dev-log.socket \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-udevd.service \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-udev-trigger.service \
    ${systemd_system_unitdir}/sysinit.target.wants/kmod-static-nodes.service \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-tmpfiles-setup.service \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-tmpfiles-setup-dev.service \
    ${systemd_system_unitdir}/sysinit.target.wants/systemd-sysctl.service \
    \
    ${systemd_system_unitdir}/ctrl-alt-del.target \
    ${systemd_system_unitdir}/reboot.target \
    ${systemd_system_unitdir}/systemd-reboot.service \
    ${systemd_system_unitdir}/syslog.socket \
    \
    ${systemd_system_unitdir}/slices.target \
    ${systemd_system_unitdir}/system.slice \
    ${systemd_system_unitdir}/-.slice \
    \
    ${libdir}/sysctl.d/50-default.conf \
    ${libdir}/tmpfiles.d/systemd.conf \
    ${libdir}/udev/rules.d \
    ${libdir}/udev/ata_id \
    ${libdir}/udev/cdrom_id \
    ${libdir}/udev/scsi_id \
    \
    ${bindir}/journalctl \
    ${bindir}/systemctl \
    ${bindir}/systemd-ask-password \
    ${bindir}/systemd-run \
    ${bindir}/systemd-escape \
    ${bindir}/systemd-cgls \
    ${bindir}/systemd-tmpfiles \
    ${bindir}/systemd-tty-ask-password-agent \
    ${bindir}/udevadm \
    \
    ${systemd_system_unitdir}/initrd.target \
    ${systemd_system_unitdir}/initrd-fs.target \
    ${systemd_system_unitdir}/initrd-root-device.target \
    ${systemd_system_unitdir}/initrd-fs.target \
    ${systemd_system_unitdir}/initrd-root-fs.target \
    ${systemd_system_unitdir}/initrd-switch-root.target \
    ${systemd_system_unitdir}/initrd-switch-root.service \
    ${systemd_system_unitdir}/initrd-cleanup.service \
    ${systemd_system_unitdir}/initrd-udevadm-cleanup-db.service \
    ${systemd_system_unitdir}/initrd-parse-etc.service \
    \
    ${localstatedir}/log \
    ${localstatedir}/volatile/log \
    /init \
    /sysroot \
    \
    ${systemd_unitdir}/systemd/systemd-sulogin-shell \
"
