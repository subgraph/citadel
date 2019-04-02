
: ${DEBIAN_MIRROR:="https://deb.debian.org/debian"}
: ${DEBIAN_RELEASE:="buster"}

BASE_PACKAGES="iproute2 less xz-utils sudo dbus libpam-systemd openssh-client packagekit-gtk3-module libcanberra-gtk3-module libpulse0 fonts-roboto-hinted bash-completion"

BASE_PRE_INSTALL_MODULES="utility-library configure-locale create-user set-hostname no-install-recommends write-apt-sources"

BASE_POST_INSTALL_MODULES="loginctl-enable-linger configure-systemd install-configure-host0 install-launch-script setup-theme-symlinks apt-cacher-ng add-xsettings-schema"

trap "exit 1" TERM
export TOP_PID=$$

fatal() {
    >&2 echo "Fatal: ${1}"
    kill -s TERM $TOP_PID
}

info() {
    printf "[+] ${1}\n"
}
