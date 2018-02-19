
# How to make rootfs writable

1. Open Citadel terminal

2. Su to root

    $ su

3. Remount root as read-write

    # mount -o remount,rw /

# How to change timezone

1. Make rootfs writable

2. Run Setting application in Gnome, change timezone in Details -> Date & Time

# How to change Gnome lock screen passwd

1. Open Citadel terminal

2. Generate new password with openssl

    $ openssl passwd
    Password: 
    Verifying - Password: 
    sGYyWXqDuh64g

3. Su to root

    $ su

4. Make rootfs writable

    # mount -o remount,rw /

5. Copy new password hash into /etc/shadow

    # vim /etc/shadow

# How to install image update

1. Open Citadel terminal

2. Su to root

3. Determine if current boot is from rootfsA or rootfsB.  Make sure you don't overwrite the currently mounted rootfs partition!

    # findmnt /
    TARGET SOURCE                      FSTYPE OPTIONS
    /      /dev/mapper/citadel-rootfsA ext2   rw,relatime,errors=continue,user_xattr

4. Locate the rootfs update image you want to install

    # file /storage/user-data/primary-home/citadel-image-intel-corei7-64.ext2 
    /storage/user-data/primary-home/citadel-image-intel-corei7-64.ext2: Linux rev 1.0 ext2 filesystem data, UUID=d9dd20e9-9286-4c60-9dc3-37c68e36481c (large files)

5. Write to the correct partition with dd command.

    # dd if=/storage/user-data/primary-home/citadel-image-intel-corei7-64.ext2 of=/dev/mapper/citadel-rootfsB bs=4M
    255+1 records in
    255+1 records out
    1071823872 bytes (1.1 GB, 1022 MiB) copied, 3.01726 s, 355 MB/s

6. Sync just to be sure everything is flushed to disk, then reboot into new image.

    # sync
    # reboot

# How to have hardware graphics acceleration for applications

1. Open Citadel terminal

2. Su to root

3. Make rootfs writable

    # mount -o remount,rw /

4. Enable /dev/dri/renderD128 bind mount in primary.nspawn file

    # vim /etc/systemd/nspawn/primary.nspawn

# How to use Qemu?

1. Open Citadel terminal

2. Su to root

3. Make rootfs writable

    # mount -o remount,rw /

4. Enable /dev/kvm bind mount in primary.nspawn file

    # vim /etc/systemd/nspawn/primary.nspawn

