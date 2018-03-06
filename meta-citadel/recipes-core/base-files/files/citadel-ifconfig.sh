#!/bin/bash
#
# Called from /etc/udev/rules.d/citadel-network.rules to configure
# external network interfaces and the vz-clear bridge which is created
# automatically by systemd-nspawn when --network-zone=clear (or Zone=clear)
# option is used to launch a container.
# 
# Both the bridge device and external interfaces are masqueraded so that
# container veth instances added to the bridge will work.
#
# TODO: External interfaces need to have a set of filering rules applied.  
# The filtering rules should go in a separate script file in a more visible
# location such as /usr/share/citadel/citadel-firewall.sh
#

VZ_CLEAR_ADDRESS="172.17.0.1/24"

# add NAT rule for external interfaces and also for vz-clear bridge

iptables -t nat -A POSTROUTING -o ${1} -j MASQUERADE

if [[ ${1} == "vz-clear" ]]; then
    ip addr add ${VZ_CLEAR_ADDRESS} dev vz-clear
    ip link set vz-clear up
    exit 0
fi

