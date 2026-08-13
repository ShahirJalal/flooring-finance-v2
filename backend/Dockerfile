# --- Build stage ---
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Leverage Docker layer caching: resolve dependencies before copying source.
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN apk add --no-cache bash && ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/finance.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
