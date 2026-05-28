```bash
#!/bin/bash

echo "Starting Scooter Rental System..."

echo "Checking Docker..."

if ! docker info >/dev/null 2>&1; then
    echo "Docker is not running."

    if command -v systemctl >/dev/null 2>&1; then
        echo "Trying to start Docker service..."

        sudo systemctl start docker

        echo "Waiting for Docker Engine..."

        until docker info >/dev/null 2>&1; do
            sleep 5
        done
    else
        echo "Please start Docker manually."
        exit 1
    fi
fi

echo "Docker is ready."

echo "Stopping old containers if they exist..."
docker compose down

echo "Building and starting containers..."
docker compose up --build

if [ $? -ne 0 ]; then
    echo "Application failed to start."
    exit 1
fi
```
