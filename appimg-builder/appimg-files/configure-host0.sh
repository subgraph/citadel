#!/bin/bash

# inspired by last section of
# 
#     https://www.freedesktop.org/wiki/Software/systemd/ContainerInterface/
#
SYSTEMD_ENV=$(xargs -a /proc/1/environ --null echo)

process_var() {
    case ${1} in
        "IFCONFIG_IP")
            ip addr add ${2} dev host0
            ip link set host0 up
            ;;
        "IFCONFIG_GW")
            ip route add default via ${2}
            ;;
    esac
}

for var in ${SYSTEMD_ENV}; do
    IFS="=" read -a PAIR <<< ${var}
    if [[ ${#PAIR[@]} -eq 2 ]]; then
        process_var ${PAIR[0]} ${PAIR[1]}
    fi
done
