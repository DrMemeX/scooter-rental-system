#!/bin/bash

echo "Starting Scooter Rental System..."

echo "Checking Docker..."

if ! docker info > /dev/null 2>&1; then
  echo "Docker is not running."
  echo "Please start Docker Desktop or Docker Engine and run this script again."
  exit 1
fi

echo "Docker is ready."

echo "Starting PostgreSQL..."
docker compose up -d

echo "Waiting for PostgreSQL..."
sleep 5

echo "Checking port 8080..."

PID=$(lsof -ti :8080)

if [ -n "$PID" ]; then
  echo "Port 8080 is already in use by PID $PID"
  echo "Stopping process..."
  kill -9 $PID
fi

echo "Building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
  echo "Build failed."
  exit 1
fi

echo "Starting application..."
mvn spring-boot:run -pl web-module