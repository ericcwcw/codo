# Codo


### Get started
```sh
// start postgres and redis
docker-compose up -d

// build and start spring boot
./gradlew build
java -jar ./build/libs/codo-0.0.1-SNAPSHOT.jar

// start integration test with testcontainers (postgres, redis) and docker over limactl
DOCKER_HOST=unix:///Users/<username>/.lima/docker/sock/docker.sock TESTCONTAINERS_RYUK_DISABLED=true ./gradlew test
```