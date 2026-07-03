# syntax=docker/dockerfile:1
#
# Build context must be the repository root (this module belongs to a multi-module
# Maven reactor and inherits from ../pom.xml):
#   docker build -f eureka-server/Dockerfile -t etiya/eureka-server .

# ---- Build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Parent POM + this module's POM only, so dependency resolution is cached in its
# own layer and isn't invalidated by unrelated source changes.
COPY pom.xml ./
COPY eureka-server/pom.xml eureka-server/pom.xml
RUN mvn -f eureka-server/pom.xml -B dependency:go-offline

COPY eureka-server/src eureka-server/src
RUN mvn -f eureka-server/pom.xml -B -DskipTests clean package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/eureka-server/target/eureka-server-*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
