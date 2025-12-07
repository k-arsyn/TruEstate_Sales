FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/retail-backend-0.0.1-SNAPSHOT.jar app.jar

# Copy the CSV file to the same directory as the JAR
COPY --from=build /app/src/main/resources/sales_data.csv ./sales_data.csv

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application with classpath pointing to current directory
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dloader.path=. -jar app.jar"]
