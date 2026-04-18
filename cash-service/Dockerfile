# Use OpenJDK as base image (choose appropriate version)
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Set default profile via environment variable
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the Spring Boot JAR from build directory
COPY /target/*.jar app.jar

# Expose the application port (default Spring Boot is 8080)
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]