Citadel Realms
==============

In Citadel applications are installed into and run from spaces called Realms.  Realms
are implemented internally as systemd-nspawn containers but you don't need to know
this because the `realms` command-line tool manages creating and launching containers
for you.


### Application Images

The root filesystem for realms are called Application Images, but we usually just call them
appimgs. We have created a framework for constructing these images from Debian based distributions 
which we use to build the default appimg that we provide, but we
also encourage users to experiment with building their own images.

#### Tree Application Images

Tree images are the only type of application image which is currently implemented for realms.

The rootfs is a tree of files on the filesystem, and also it is a btrfs subvolume.

#### Block Application Images



### `default` realm

One realm is always selected to be the `default` realm.  The default realm
starts automatically when the system boots.  The `realms` utility can be used
to change which realm is the default realm. Switching the default realm changes 
the symlink `/realm/default.realm` to point to a different realm instance directory.

    citadel:~# realms default
    Default Realm: main

    citadel:~# realms default project
    [+] default realm changed from 'main' to 'project'

    citadel:~# realms default
    Default Realm: project

### `current` realm

If any realms are running, then one realm is always the `current` realm. The current
realm is a realm that is being monitored by the `citadel-desktopd` daemon.  This
daemon is responsible for safely copying application `.desktop` files from the running
realm instance to a temporary directory where they will be read by the GNOME desktop to
to display a menu of applications that can be launched.

Changing the `current` realm, changes the set of applications which are visible to 
gnome-shell to only the applications installed in this realm.  Also, any applications 
started by gnome-shell will run in the `current` realm.

    citadel:~# realms 
    Current Realm: main
    
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

#### `/realms/config` file

This file is a template of the configuration file for individual realms.  When a new realm is created this file in copied into the new realm instance directory.  By modifying this file, the default configuration for new realm instances can be changed.

#### `/realms/Shared` directory

This directory is bind mounted to `/home/user/Shared` of each running realm that has the option `use-shared-dir` enabled.  It's a convenient way to move files between different realms and between citadel and realms.

#### `/realms/skel` directory

Files which are added to this directory will be copied into the home directory of any newly created realm.  The directory is copied as a tree of files and may contain subdirectories.  

#### `/realms/default.realm`

A symlink which points to a realm instance directory of the default realm.  The default realm is the realm which starts when the system is booted.
 
#### `/realms/realm-$name`

This is a realm instance directory, for a realm with $name as the realm name.

    /realm-main
        config
        /home
        /rootfs

 * `config`  : configuration file copied from `/realms/config`
 * `/home`   : directory mounted to `/home/user` in the realm, populated from `/realms/skel`
 * `/rootfs` : btrfs subvolume clone (snapshot) of an application image.
    

#### Realm instance directory layout

    /realm-main
        config
        /home
        /rootfs

