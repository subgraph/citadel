#
# Base set of packages, should not include anything needed only on desktop
#
inherit packagegroup

RDEPENDS_${PN} = "\
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
    bash-completion \
    grep \
    procps \
    psmisc-extras \
    tar \
    pciutils \
    sysfsutils \
    polkit \
    wpa-supplicant \
    networkmanager \
    kernel-modules \
    packagegroup-firmware \
    vim \
    vifm \
    citadel-user \
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
    iproute2-bash-completion \
    glib-2.0-bash-completion \
    pulseaudio-bash-completion \
    systemd-bash-completion \
    util-linux-bash-completion \
"
