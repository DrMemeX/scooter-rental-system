echo Starting Scooter Rental System...

echo.
echo [1/3] Starting PostgreSQL container...
docker compose up -d

echo.
echo Waiting for database startup...
timeout /t 5 > nul

echo.
echo [2/3] Building project...
call mvn clean install -DskipTests

echo.
echo [3/3] Starting Spring Boot application...
call mvn spring-boot:run -pl web-module

pause