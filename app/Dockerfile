# build phase
FROM maven:3.9-eclipse-temurin-21 AS build

# Define workdir
WORKDIR /app

# Copy pom.xml file and dependencies to cache
COPY pom.xml ./
COPY private.pem /app
COPY public.pem /app

# Copy source-code to workdir
COPY application /app/application
COPY auth /app/auth
COPY core /app/core
COPY user /app/user

RUN mvn dependency:go-offline

# Project copile and create final JAR
RUN mvn clean package

# Final phase
FROM openjdk:21-ea-25-jdk-oraclelinux8

# Define workdir
WORKDIR /app

# Copy the generated JAR to workdir
COPY --from=build /app/application/target/application-0.0.1-SNAPSHOT.jar /app/application/application-0.0.1-SNAPSHOT.jar

# Expose port
EXPOSE 8080

# Define entry point command
ENTRYPOINT ["java", "-jar", "./application/application-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=docker-local"]
