# =====================================================================
# Multi-stage Dockerfile for Render / Railway / Koyeb deployment
# Stage 1: Build the Spring Boot executable JAR with Maven & JDK 21
# Stage 2: Run the packaged JAR on a slim JRE with 512MB RAM tuning
# =====================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compile and package application (skip tests during container build)
COPY src ./src
RUN mvn -B package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/student-management-system-1.0.0.jar app.jar

EXPOSE 8080

# Memory flags tuned for Render's 512MB Free Web Service tier
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-jar", "app.jar"]
