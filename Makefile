IMAGE_NAME=bankcards-app:latest


build:
	./mvnw clean package spring-boot:build-image -Dspring-boot.build-image.imageName=$(IMAGE_NAME)


up:
	docker compose up -d


down:
ifeq ($(VOLUMES),true)
	docker compose down -v
else
	docker compose down
endif

rebuild: build down up

