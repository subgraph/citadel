#
# Base set of packages, should not include anything needed only on desktop
#

inherit packagegroup

BASH_COMPLETION = "\
    bash-completion \
    iproute2-bash-completion \
    glib-2.0-bash-completion \
    pulseaudio-bash-completion \
    systemd-bash-completion \
    util-linux-bash-completion \
"

RDEPENDS_${PN} = "\
    citadel-config \
    base-files \
    base-passwd \
    systemd \
    keymaps \
    kbd \
    console-tools \
    coreutils \
    gzip \
    less \
    util-linux \
    net-tools \
    iputils \
    which \
    parted \
    bash \
    ${BASH_COMPLETION} \
    grep \
    procps \
    psmisc-extras \
    tar \
    pciutils \
    sysfsutils \
    wpa-supplicant \
    kernel-modules \
    packagegroup-firmware \
    vim \
    vifm \
    nano \
    tzdata \
    tzdata-americas \
    tzdata-asia \
    tzdata-europe \
    tzdata-posix \
    glibc-charmap-utf-8 \
    lvm2 \
    findutils \
    lsof \
    strace \
    iproute2 \
    util-linux-hwclock \
    btrfs-tools \
    systemd-analyze \
    wget \
    sed \
    xz \
    openssh-ssh \
    cryptsetup \
    e2fsprogs \
    dosfstools \
    libpam \
    wireguard-tools \
    resolvconf \
    udisks2 \
    efivar \
    efibootmgr \
    iw \
"
