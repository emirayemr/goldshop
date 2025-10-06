FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Maven wrapper ve bağımlılıklar
COPY mvnw .
COPY .mvn .mvn
# Windows'tan geliyorsa CRLF temizle ve çalıştırılabilir yap
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Uygulama kaynakları
COPY src src

# Build
RUN ./mvnw clean package -DskipTests

# Port ve çalıştırma
EXPOSE 8080
CMD ["sh", "-lc", "java -jar target/*.jar"]
