.PHONY: docker-image docker-shell

BASE_DIR = $(shell pwd)
BASE_BINDMOUNT = type=bind,source=$(BASE_DIR),target=/home/builder/citadel

all: docker-image docker-shell

docker-image:
	docker build -t citadel-builder scripts/docker

docker-shell:
	docker run -it --mount $(BASE_BINDMOUNT) citadel-builder 

user-rootfs:
	mkdir -p build/debootstrap
	docker run -it --privileged --mount $(BASE_BINDMOUNT) citadel-builder sudo scripts/build-user-rootfs-stage-one | tee build/debootstrap/build-user-rootfs.log

