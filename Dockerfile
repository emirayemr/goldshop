# Use official lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Maven wrapper files and set executable permission
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy project files
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src

# Build Spring Boot app
RUN ./mvnw clean package -DskipTests

# Expose port and run
EXPOSE 8080
CMD ["java", "-jar", "target/*.jar"]
