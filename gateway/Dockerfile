# Use OpenJDK as base image (choose appropriate version)
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR from build directory
COPY /target/*.jar app.jar

# Expose the application port (default Spring Boot is 8080)
EXPOSE 9080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]