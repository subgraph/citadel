.PHONY: docker-image docker-shell

BASE_DIR = $(shell pwd)

all: docker-image docker-shell

docker-image:
	docker build -t citadel-builder scripts/docker

docker-shell:
	docker run -it --mount type=bind,source=$(BASE_DIR),target=/home/builder/citadel citadel-builder 

