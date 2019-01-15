
do_install_append() {
    echo "a4e415feff81466c925aab34b0c35a3c" > ${D}${sysconfdir}/machine-id
}
