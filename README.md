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

To build a full citadel image:

    $ make citadel-image

The build will take several hours the first time, but for later builds the build system will use cached artifacts stored 
in `citadel/build/sstate-cache` for components that have not changed and new builds will usually only take a few minutes.

## Some Assembly Required

Currently there are some rather unreliable scripts to make it possible to turn build output into something that you can install and run.

Very soon these scripts will be replaced by an actual installer that you can just build by running a make target, but that doesn't quite exist yet.

### Running `scripts/create_install_pack` to create installpack.tar

Before creating the installpack, some artifacts must exist in the build/images directory:

  * `make citadel-image`  Creates: `images/citadel-image-intel-corei7-64.ext2`
  * `make citadel-kernel` Creates: `images/bzImage`
  * `make bootloader`     Creates: `images/systemd-bootx64.efi`
  * `make user-rootfs`    Creates: `debootstrap/user-rootfs.tar.xz`

After all of those components have been build, you can run `scripts/create_install_pack` which will create a file in the current directory called `installpack.tar`.  

You can then unpack this tarball somewhere and run a script inside of it called `install.sh` to install to a USB stick (do this first, at least until you understand the process) or to install to internal disk drive.

    $ tar xvf installpack.tar
    $ cd installpack
    $ sudo ./install.sh /dev/sdb

The install.sh script redirects all output from the commands it runs to a file install.log in the current directory.  If the last line of output does not say "Install completed successfully" then something failed.  Look in install.log for information about what went wrong.  The script itself does not print any output when it fails, it will just stop at one of the steps and it appears as if everything worked since there is no error output.


