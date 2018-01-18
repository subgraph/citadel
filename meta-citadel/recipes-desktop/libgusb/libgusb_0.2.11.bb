SUMMARY = "GObject wrapper for libusb1"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=2d5025d4aa3495befef8f17206a5b0a1"

SRC_URI = "https://people.freedesktop.org/~hughsient/releases/libgusb-${PV}.tar.xz"
SRC_URI[md5sum] = "fa2b41b828c749f9190edf888948a77b"
SRC_URI[sha256sum] = "9cb143493fab1dc3d0d0fdba2114b1d8ec8c5b6fad05bfd0f7700e4e4ff8f7de"

DEPENDS = "glib-2.0 libusb1"

inherit pkgconfig autotools gobject-introspection

EXTRA_OECONF = "--disable-static --disable-gtk-doc"
BBCLASSEXTEND="native"
