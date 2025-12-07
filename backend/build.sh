#!/usr/bin/env bash
# Build script for Render

echo "Building Spring Boot application..."
./mvnw clean package -DskipTests

echo "Build complete!"

