Citadel Realms
--------------

Citadel contains only the base operating system and the Gnome desktop, it does not
include any applications. To be able to install and run applications Citadel can
create spaces which are called Realms.

A Realm is a container similar to a Docker or LXC container in which any Linux
distribution could be installed.  We use a Debian based image but it would not be
difficult to create an image for another Linux distribution.

The realm containers are launched with systemd-nspawn but this is a detail of
how they are implemented and not something it is necessary to learn about in order to use them.

Citadel provides a command-line tool `realms` for creating, managing, and launching Realm instances.

### The `default` realm

One realm is always selected to be the `default` realm.  This realm
starts automatically when the system boots.  The `realms` utility can be used
to change which realm is the default realm. Switching the default realm changes
the symlink `/realm/default.realm` to point to a different realm instance directory.

    citadel:~# realms default
    Default Realm: main

    citadel:~# realms default project
    [+] default realm changed from 'main' to 'project'

    citadel:~# realms default
    Default Realm: project

### The `current` realm

Multiple realms may be launched at once but the Gnome Desktop is only associated with
one of the running realms.  This realm is called the `current` realm.

When displaying applications available to launch from the desktop, Gnome will only
be aware of applications that are installed in the realm which is set as `current`
and any application launched from the desktop will run inside this current realm.

Setting another realm as current does not affect any applications that are already running.
Changing the current realm only means that any further applications which are launched
will now run in the newly chosen realm.

Changing or querying the current realm is done with the `realms current` command, and
if you choose a realm which is not currently running it will be automatically started.

    citadel:~# realms current
    Current Realm: main

    citadel:~ # realms current project
    [+]: Started realm 'project'
    [+]: Realm 'project' set as current realm

    citadel:~ # realms current
    Current Realm: project

Underneath the hood, this command just changes the symlink `/run/realms/current.realm` to
point to a new realm.  This directory is monitored for changes with `inotify` and when
the symlink changes a new set of `.desktop` files is swapped into a temporary directory
where Gnome will look for metadata about which applications are installed.

### Creating a new realm

New realms are created with the command `realms new <realm name>`

When a new realm is created a btrfs snapshot of some application image is created at
`/realms/realm-$name/rootfs`.  By default it is the base image (`base.appimg`) which
is cloned as a snapshot.  Application images are described in detail in a later section.

    citadel:~ # realms new project
    [+]: Populating realm home directory with files from /realms/skel
    Create a snapshot of '/storage/appimg/base.appimg' in '/realms/realm-project/rootfs'

A new empty home directory is also created for the realm instance.  Any file which are placed
into the `/realm/skel` directory will be copied into any newly created realm home directory.


### Realms configuration file

All of the curretly supported configuration options are listed below with their default values assigned.

    use-shared-dir = true
    use-sound = true
    use-x11 = true
    use-wayland = true
    use-gpu = false
    use-kvm = false
    use-network = true
    network-zone = "clear"

If you wish to change any of these options to something other than what is listed above add the
corresponding line to the file `/realms/realm-$name/config`

    citadel:~ # echo "use-gpu = true" > /realms/realm-main/config

#### Option `use-shared-dir`

Set to `false` to disable mounting the shared directory `/realms/Shared` into this realm at
`/home/user/Shared`.

#### Option `use-sound`

Set to `false` to prevent mounting pulse audio socket and sound device into this realm.

#### Option `use-x11`

Set to `false` to prevent mounting `/tmp/.X11-unix` into the realm. This is the socket for communicating
with the `XWayland` X11 compatibility daemon.

#### Option `use-wayland`

Set to `false` to prevent mounting the wayland display server socket `/run/user/1000/wayland-0`
 into the realm.

#### Option `use-gpu`

Set to `true` to mount the device `/dev/dri/renderD128` into the realm. Adding this
device will make hardware graphics acceleration available to applications running
in the realm.

#### Option `use-kvm`

Set to `true` to mount the device `/dev/kvm` into the realm.  This will make it
possible to run Qemu and other KVM based tools with hardware virtualization
inside the realm.

#### Option `use-network`

Set to `false` to disable configuring the realm with access to the internet.  The
realm instance will only have a localhost network interface.

#### Option `network-zone`

Setting a name here will create bridge device in citadel with the name vz-$name if
it doesn't already exist and attach this realm instance to that bridge.

### Realms base directory layout

The realms base directory is stored on the storage partition at `/storage/realms` and is bind mounted to `/realms` on the root filesystem for convenience.

    /realms
        config
        /Shared
        /skel
        /default.realm -> realm-main
        /realm-main
        /realm-project
        /realm-testing

#### File `/realms/config`

This file is a template of the configuration file for individual realms. When a new
realm is created this file in copied into the new realm instance directory. By
modifying this file, the default configuration for new realm instances can be changed.

#### Directory `/realms/Shared`

This directory is bind mounted to `/home/user/Shared` of each running realm that has
the option `use-shared-dir` enabled.  It's a convenient way to move files between
different realms and between citadel and realms.

#### Directory `/realms/skel`

Files which are added to this directory will be copied into the home directory of
any newly created realm.  The directory is copied as a tree of files and may contain
subdirectories.

#### Symlink `/realms/default.realm`

A symlink which points to a realm instance directory of the default realm.  The
default realm is the realm which starts when the system is booted.

#### Directory `/realms/realm-$name`

This is a realm instance directory, for a realm with $name as the realm name.

    /realm-main
        config
        /home
        /rootfs

 ##### `config`

Configuration file for the realm instance copied from `/realms/config` or
created by the user.

##### `/home`

Home directory for this realm. It will be mounted to `/home/user` in
the realm instance.

##### `/rootfs`

The root filesystem of this realm. It is cloned from (a btrfs subvolume snapshot of)
some application image.

### Application Images

(Not to be confused with the [AppImage](https://appimage.org) packaging system)

The root filesystem for realms are called Application Images but we often use
the shorter name *appimg*.

We have created [a framework](https://github.com/subgraph/citadel/tree/master/appimg-builder)
for building a Debian based images and we use this to build the default appimg that we ship.

We also encourage users to experiment with building their own custom images.


**Tree Application Images** are the only type of application image which are currently implemented for realms.

The rootfs is a tree of files on the filesystem, and it is also a btrfs subvolume
which is cloned at zero cost (internally with `btrfs subvolume snapshot`) to use
as the root filesystem of newly created realms.


#### Block Application Images (and also Sealed Application Images)

In the future we will add another type of application image called a **Block
Application Image**. This type of image will be stored as a disk volume image file
and will be mounted with a loop device rather than existing as a tree of files on the
filesystem.

This will make it possible to enforce [dm-verity](https://www.kernel.org/doc/Documentation/device-mapper/verity.txt)
verification over the image and ensure that no malicous or unintended modifications
can be made to any of the the files on the root filesystem. Signature verification
over the dm-verity root hash is done from the citadel rootfs image which is also
secured with dm-verity.  When enforcement of boot integrity is also implemented this
will create a chain of cryptographic assurances that no component of the system has
been tampered with.

Block images with signatures and dm-verify verification enabled are called **Sealed Application Images**

### Updating an Application Image

To modify or update an application image run the `realms update-appimg` command.
A container will be created for updating the image and a root shell session will
open. From this session regular package management commands can be run.  Any changes
made will only affect future realms created from this appimg.

    citadel:~ # realms update-appimg
    [+]: Entering root shell on base appimg
    root@base-appimg-update:/# apt update

    [...]
