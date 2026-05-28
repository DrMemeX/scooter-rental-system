@echo off

echo Starting Scooter Rental System...

echo Checking Docker...

docker info >nul 2>&1

if %errorlevel% neq 0 (
    echo Docker is not running. Trying to start Docker Desktop...

    if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" (
        start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
    ) else if exist "%ProgramFiles(x86)%\Docker\Docker\Docker Desktop.exe" (
        start "" "%ProgramFiles(x86)%\Docker\Docker\Docker Desktop.exe"
    ) else (
        echo Docker Desktop not found.
        pause
        exit /b 1
    )

    echo Waiting for Docker Engine...

    :wait_docker
    timeout /t 5 >nul
    docker info >nul 2>&1

    if %errorlevel% neq 0 (
        goto wait_docker
    )
)

echo Docker is ready.

echo Stopping old containers if they exist...
docker compose down

echo Building and starting containers...
docker compose up --build

if %errorlevel% neq 0 (
    echo Application failed to start.
    pause
    exit /b 1
)

pause