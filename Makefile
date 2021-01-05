.PHONY: build clean run-postgres

build:
	./gradlew shadowJar

clean:
	./gradlew clean

run-postgres:
	docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=123 postgres:12