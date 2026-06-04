# Build — Debian-based JDK (Alpine often breaks Maven wrapper / runs OOM on Render)
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

ENV MAVEN_OPTS="-Xmx768m"

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -DskipTests

COPY src src

RUN ./mvnw package -DskipTests -B

# Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/formvity-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
