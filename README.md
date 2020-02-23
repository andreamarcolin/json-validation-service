# JSON Validation Service
A simple RESTful microservice which allows to validate JSON documents against user supplied JSON Schemas.
Based on a pure FP Scala stack (zio, cats, doobie, circe, pureconfig)

## Requirements
- sbt
- Docker
- docker-compose
 
## Packaging the service
To package the app as a Jar, launch sbt from the repository root with the following command: 
```bash
sbt packageJar
```

## Running the service locally
The project ships with a docker-compose file, which will pull and start service dependencies (namely PostgreSQL) 
It will also preload some SQL scripts to add a db schema and db tables.

Finally, it will build a docker image for the project bundling it from a pre-assembled JAR. 

To get started from scratch, first clone this repo on your local machine, `cd` into it and follow these steps:

- Package the JAR
  ```bash
  sbt packageJar
  ```

- Run with docker-compose
  ```bash
  docker-compose up -d
  ```
 
You should now be able to reach the service at http://localhost:8080.
For example, to check app health, make a GET request like this:
```bash
curl http://localhost:8080/health
```

## Running the service locally as standalone JAR (pointing to external DB)
To run the app without docker-compose, first package it with `sbt packageJar`, then run it as a plain JAR,
providing necessary configuration overrides as environment variables.

```bash
export DB_URL=jdbc:postgresql://postgres:5432/jvs
export DB_USER=jvs
export DB_PASSWORD=jvs

java -Xms64m -Xmx256m -jar target/jvs-<version>.jar
```