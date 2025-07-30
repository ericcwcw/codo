# Codo
A set of apis for a collaborative todo list application

### Get started
```sh
// start postgres and redis
docker-compose up -d

// build and start spring boot
./gradlew build -x test
java -jar ./build/libs/codo-0.0.1-SNAPSHOT.jar

// start integration tests with testcontainers
./gradlew test
```

### Features
1. CRUD operations for todo list
2. Basic authentication
3. Collaborative todo lists
4. Role based access control based on todo list permission level
5. Email verification during user registration
6. Github action CI pipeline
7. Setup testcontainers for integration tests



1. fix verify user response text
2. 