# syntax=docker/dockerfile:1
#
# Build context must be the repository root (this module belongs to a multi-module
# Maven reactor and inherits from ../pom.xml):
#   docker build -f product-service/Dockerfile -t etiya/product-service .
#
# Note: the service's config-server-supplied port (server.port) is 0 (random) by
# default, for horizontal scaling behind Eureka. The container's actual listening
# port is whatever SERVER_PORT is set to at `docker run`/compose time; 8082 below
# is just the documented default used by infra/docker-compose.yml.

# ---- Build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Parent POM + this module's POM only, so dependency resolution is cached in its
# own layer and isn't invalidated by unrelated source changes.
COPY pom.xml ./
COPY product-service/pom.xml product-service/pom.xml
RUN mvn -f product-service/pom.xml -B dependency:go-offline

COPY product-service/src product-service/src
RUN mvn -f product-service/pom.xml -B -DskipTests clean package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/product-service/target/product-service-*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
