Application Image Builder
=========================

Application Images (or appimgs for short) are created with this builder
framework. The build is controlled by a configuration file which can customize
the build process in various ways such as adding extra packages and specifying
shell functions to be run at certain stages.

The configuration file is really just a shell script but it should follow the 
conventions described in the Configuration File section of this document.

The builder bind mounts a package cache directory into /var/cache/apt of the 
chroot so packages will only need to be downloaded once no matter how many times
you repeat the build. As a nice consequence of this the cached packages don't have 
to be removed from the final image because they are merely unmounted when the 
process completes.

Unless you disable it with a command line option, a tmpfs will be mounted on the
directory the rootfs is built on.If you are tweaking a config and making repeated 
builds this is not only a lot faster, but will also avoid hammering SSD drives with
excessive writes (and write amplification).

By default the application image builder is self-hosting and can always be
run from inside images that it creates.  Building an image is as easy as:

    mkdir work && cd work      : Make a directory to work in
    appimg-builder --new       : writes a template file build.conf in current directory
    vim build.conf             : (optionally) make some changes to the template
    sudo appimg-builder        : Build an application image

If you want you can even skip the steps of creating and editing a config file and
just run appimg-build in a work directory and it will build the default appimg we use 
with Citadel.

Stage One
---------

The Stage One builder uses debootstrap to build a very minimal debian
installation.  Then a chroot is set up and stage-two.sh is executed inside the
chroot to perform most of the installation.

Stage Two
---------

The stage-two.sh script mostly just orchestrates the execution of small
fragments of shell script code that are called 'modules'.  The base framework
modules can be found in the directory /usr/share/appimg-builder/appimg-modules.

It imports the configuration file with the 'source' command after all the key
variables and functions have been defined.  It's possible to override any of
these variables and functions simply by defining another version with the same
name in the configuration file, but you should almost never need to do this.

Configuration File
------------------

- Variables

PACKAGES can be set to a list of additional packages to add to the base set of
packages.

  PACKAGES="extremetuxracer biff anarchism" 

PRE_INSTALL_MODULES can be set to a list of modules to run before the main
package installation stage happens.  The contents will be appended to a
pre-defined list of 'base' modules that run.

  PRE_INSTALL_MODULES="my-cool-module another-module"

If complete control over the modules to run is required, you can override the
variable BASE_PRE_INSTALL_MODULES entirely rather than providing
PRE_INSTALL_MODULES.  Other modules depend on 'utility-library' so it is usually
required and should be the first module listed.

  BASE_PRE_INSTALL_MODULES="utility-library configure-locale custom-create-user"

POST_INSTALL_MODULES is a list of modules to execute after packages have been
installed.  It works exactly the same way as PRE_INSTALL_MODULES and also has a
corresponding 'base' variable that could be overidden if necessary.

- Modules

Modules can be functions that you define or they can be loaded from files on
disk. To use files rather than functions a directory named 'appimg-modules'
must exist as a subdirectory of the directory containing the configuration file.
Any files you place in this directory will be found by name during the module
execution stages.

- Installing Files

If you would like to have external files such as configuration files copied into
the image, create 'appimg-files' as a subdirectory of the directory containing
the configuration file.  You can then use the install_file command inside of a
module to copy the files from this directory. You can either store the files to
install in a flat directory or organize them into subdirectories mirroring the
location in which they will be installed.  Depending on which option you use,
the install_file command how two different modes.  In the examples below BASE
refers to the directory in which your configuration file is located.

   (1): install_file [mode] [file] [target directory]
  
   Example: Install BASE/appimg-files/my_config.conf 
              to /etc/mydaemon/my_config.conf 
  
        install_file 0644 my_config.conf /etc/mydaemon
     
   (2): install_file [mode] [full path] 
  
   Example: Install BASE/appimg-files/etc/mydaemon/my_config.conf 
              to /etc/mydaemon/my_config.conf
  
        install_file 0644 /etc/mydaemon/my_config.conf
  
