# Base image for Java 17
FROM eclipse-temurin:17-jdk-alpine

# Create app directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source
COPY src src

# Build the app
RUN ./mvnw clean package -DskipTests

# Run the app
EXPOSE 8080
CMD ["java", "-jar", "target/*.jar"]
