FROM debian:buster

ENV DEBIAN_FRONTEND noninteractive

# Install yocto dependencies, locales, libs for gcc plugin building and a text editor
RUN apt update && apt install -y gawk \
    wget \
    git-core \
    diffstat \
    unzip \
    texinfo \
    gcc-multilib \
    build-essential \
    chrpath \
    socat \
    cpio \
    python \
    python3 \
    python3-pip \
    python3-pexpect \
    xz-utils \
    debianutils \
    iputils-ping \
    locales \
    libgmp-dev \
    libmpc-dev \
    libelf-dev \
    nano \
    inkscape

# python
RUN echo "en_US.UTF-8 UTF-8" > /etc/locale.gen
RUN locale-gen
RUN update-locale LANG=en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV LC_CTYPE en_US.UTF-8

RUN useradd -ms /bin/bash builder

USER builder
RUN echo ". /home/builder/citadel/setup-build-env" >> /home/builder/.bashrc
CMD ["bash"]

WORKDIR /home/builder/citadel/

# No command is run at this point
