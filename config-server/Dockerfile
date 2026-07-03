# syntax=docker/dockerfile:1
#
# Build context must be the repository root (this module belongs to a multi-module
# Maven reactor and inherits from ../pom.xml):
#   docker build -f config-server/Dockerfile -t etiya/config-server .

# ---- Build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Parent POM + this module's POM only, so dependency resolution is cached in its
# own layer and isn't invalidated by unrelated source changes.
COPY pom.xml ./
COPY config-server/pom.xml config-server/pom.xml
RUN mvn -f config-server/pom.xml -B dependency:go-offline

COPY config-server/src config-server/src
RUN mvn -f config-server/pom.xml -B -DskipTests clean package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar

EXPOSE 8071
ENTRYPOINT ["java", "-jar", "app.jar"]
