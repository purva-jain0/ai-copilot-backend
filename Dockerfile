# 1. Use Java 21 to match your local environment
FROM eclipse-temurin:21-jdk-alpine

# 2. Set the working directory inside the cloud server
WORKDIR /app

# 3. Copy all your backend files into the server
COPY . .

# 4. Give the Maven wrapper permission to execute on Linux
RUN chmod +x mvnw

# 5. Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# 6. Expose port 8080 for Render to route traffic
EXPOSE 8080

# 7. Run the compiled jar file
ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]