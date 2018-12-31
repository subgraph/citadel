# Building Citadel

## Set up Docker

Building citadel requires that you have Docker CE installed on the build host.  The version of Docker
provided by your Linux distribution will probably not work and you should follow the following instructions
instead:

  * [Installing Docker CE on Debian](https://docs.docker.com/install/linux/docker-ce/debian/)
  * [Installing Docker CE on Fedora](https://docs.docker.com/install/linux/docker-ce/fedora/)

After installing Docker you may need to start the docker daemon.

    $ systemctl start docker

If you want the docker daemon to start automatically on boot you also need to enable it.

    $ systemctl enable docker

You may optionally add your user account to the `docker` group so that you can issue docker commands without using
sudo.  

**Warning:** This is more convenient but be careful because containers can be configured to share any file on the host. 
A user with access to the docker group can easily escalate privileges to root while the docker daemon is running.

    $ sudo usermod -aG docker $USER

## Building with Docker

A `Makefile` is provided which only contains a couple of simple targets that execute docker commands to set up and run the 
builder container.

The project uses git submodules to track openembedded layers it depends upon.  After cloning this repository you will need to 
retrieve the dependent submodules with the following command:

    $ make update-submodules

To create the builder docker image use the following command.  You only need to do this one time, but if you run it again
Docker will realize that the Dockerfile has not changed and do nothing.

    $ make docker-image

To list available make targets, run `make help` or just `make` as this is the default target:

    $ make help

To run a shell inside the docker build container:

    $ make docker-shell

The shell will run in the build directory and be configured to run build commands with `bitbake`.  

To build a full citadel installer image:

    $ make installer

The build will take several hours the first time, but for later builds the build system will use cached artifacts stored 
in `citadel/build/sstate-cache` for components that have not changed and new builds will usually only take a few minutes.

## Installer Image

If the installer build completes successfully, the installer disk image can be found in `citadel/build/images/citadel-installer.img`.

Write this file to a USB stick (for example /dev/sdb is the USB drive you want to write to):

    # dd if=citadel/build/images/citadel-installer.img of=/dev/sdb bs=4M

The installer image is a live disk from which you can run an installer program to perform a permanent installation. To
run the installer, open a citadel terminal, su to root, and run:

    # /usr/libexec/citadel-installer

You can also directly specify the disk to use on the command line.  Replace /dev/sda in the example with the actual 
disk you want to install to. You can even use the usb disk you booted the installer from!

    # /usr/libexec/citadel-installer /dev/sda

# Reproducible builds

Citadel is configured to reproducibly build artifacts. At this stage, we are 
not yet at 100% reprodubility. We have included a script to track the gaps
in reproducibility. 

This requires performing two independent builds of Citadel (preferably on the 
same host, doing this on different hosts is an exercise left up to the user).

The first build is the test build and the second build is the control build.
To compare the two, run the `repro_gaps.sh` script in the `scripts` directory
of this repo. 

By default, it will produce a summary table showing how many packages have been
built reproducibly across the test and control builds. For example:
```
$ ./repro_gaps.sh /home/user/src/citadel /home/user/src/citadel-control
```

To generate a list of the packages that were not reproducible, the script
can be run with the `--output` option:
```
$ ./repro_gaps.sh /home/user/src/citadel /home/user/src/citadel-control --output gaps.txt
```


