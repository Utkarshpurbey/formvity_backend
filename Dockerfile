# Build stage — Maven + Java 21 (matches pom.xml)
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/formvity-0.0.1-SNAPSHOT.jar app.jar

# Render sets PORT; Spring reads server.port=${PORT}
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
