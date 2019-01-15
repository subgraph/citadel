.PHONY: help docker-image docker-shell citadel-image citadel-kernel user-rootfs update-submodules build-appimg install-build-deps installer

BASE_DIR = $(shell pwd)
BASE_BINDMOUNT = type=bind,source=$(BASE_DIR),target=/home/builder/citadel
DOCKER_RUN = docker run -it --mount $(BASE_BINDMOUNT) citadel-builder
DOCKER_RUN_PRIV = docker run -it --privileged --mount $(BASE_BINDMOUNT) citadel-builder

ifdef REALM_NAME
undefine DOCKER_RUN
undefine DOCKER_RUN_PRIV
endif

APPIMG_TARFILE = build/appimg/appimg-rootfs.tar.xz
INSTALLER_IMAGE = build/images/citadel-installer.img

#
# https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
# 
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
	| awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

docker-image: ## Create docker builder image.  You need to run this one time before running anything else.
	docker build -t citadel-builder scripts/docker

docker-shell: ## Open an interactive shell in the docker build container configured for running bitbake commands.
	$(DOCKER_RUN)

installer: ${APPIMG_TARFILE} ## Build citadel installer image
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake --continue citadel-installer-image"
	@echo "Installer image:"
	@ls -l $(INSTALLER_IMAGE)

rootfs: ## Build citadel rootfs image with bitbake 
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake citadel-rootfs-image"

citadel-kernel: ## Build citadel-kernel with bitbake
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake citadel-kernel"

build-appimg: ## Build an application image
	$(DOCKER_RUN_PRIV) bash -c 'sudo APPIMG_BUILDER_BASE=$${PWD}/appimg-builder appimg-builder/stage-one.sh --no-confirm -z -d build/appimg'

fetch-all: ## Download all source packages needed for build in advance
	mkdir -p build/conf
	echo 'BB_NUMBER_THREADS="2"' > build/conf/fetch-prefile.conf
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake --read=conf/fetch-prefile.conf --continue --runall=fetch citadel-installer-image"

installer-test: ## Boot installer image with Qemu
	@scripts/qemu-boot installer

kernel-test: ## Boot kernel with Qemu ('ctrl-a x' to exit qemu)
	@scripts/qemu-boot kernel

update-submodules: ## Retrieve or update submodule projects
	git submodule update --init

install-build-deps:
	sudo apt install --no-install-recommends build-essential python bzip2 cpio chrpath diffstat file texinfo inkscape libgmp-dev libmpc-dev libelf-dev gawk

$(APPIMG_TARFILE):
	$(DOCKER_RUN_PRIV) bash -c 'sudo APPIMG_BUILDER_BASE=$${PWD}/appimg-builder appimg-builder/stage-one.sh --no-confirm -z -d build/appimg'

$(INSTALLER_IMAGE): $(APPIMG_TARFILE)
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake citadel-installer-image"
