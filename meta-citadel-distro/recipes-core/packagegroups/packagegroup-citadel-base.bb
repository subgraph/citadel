
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
"
