#!/bin/bash

echo "Starting PostgreSQL..."
docker compose up -d

echo "Waiting for database startup..."
sleep 5

echo "Building project..."
mvn clean install -DskipTests

echo "Starting application..."
mvn spring-boot:run -pl web-module