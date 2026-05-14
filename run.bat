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

echo Starting PostgreSQL...
docker compose up -d

echo Waiting for PostgreSQL...
timeout /t 5 >nul

echo Checking port 8080...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Port 8080 is already in use by PID %%a
    echo Stopping process...
    taskkill /PID %%a /F >nul 2>&1
)

echo Building project...
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo Build failed.
    pause
    exit /b 1
)

echo Starting application...
call mvn spring-boot:run -pl web-module

pause