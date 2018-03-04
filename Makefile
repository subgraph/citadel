.PHONY: help docker-image docker-shell citadel-image citadel-kernel user-rootfs update-submodules

BASE_DIR = $(shell pwd)
BASE_BINDMOUNT = type=bind,source=$(BASE_DIR),target=/home/builder/citadel
DOCKER_RUN = docker run -it --mount $(BASE_BINDMOUNT) citadel-builder  
DOCKER_RUN_PRIV = docker run -it --privileged --mount $(BASE_BINDMOUNT) citadel-builder  

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

citadel-image: ## Build citadel-image with bitbake 
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake citadel-image"

citadel-kernel: ## Build citadel-kernel with bitbake
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake citadel-kernel"

bootloader: ## Build systemd-boot
	$(DOCKER_RUN) bash -c "source setup-build-env && bitbake systemd-boot"

user-rootfs: ## Build user-rootfs tarball with debootstrap and configuration scripts
	mkdir -p build/debootstrap
	$(DOCKER_RUN_PRIV) sudo scripts/build-user-rootfs-stage-one | tee build/debootstrap/build-user-rootfs.log

update-submodules: ## Retrieve or update submodule projects
	git submodule update --init
