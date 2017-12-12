
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
    openssh-sshd \
    kernel-modules \
    packagegroup-firmware \
    vim \
    vifm \
    setcolors \
"
