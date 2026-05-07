# Build phase
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY application /app/application
COPY auth /app/auth
COPY core /app/core
COPY user /app/user
RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

# Final phase
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Instala AWS CLI via apt
RUN apt-get update && \
    apt-get install -y unzip curl && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/application/target/application-0.0.1-SNAPSHOT.jar \
     /app/application/application-0.0.1-SNAPSHOT.jar

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
