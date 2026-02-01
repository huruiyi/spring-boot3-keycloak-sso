@echo off
echo ========================================
echo Starting SSO Demo Applications...
echo ========================================
echo.
echo [1/3] Starting App A (Port 10000)...
cd app-a
start cmd /k "mvn spring-boot:run"

echo [2/3] Starting App B (Port 20000)...
cd ..\app-b
start cmd /k "mvn spring-boot:run"

echo [3/3] Waiting for applications to start...
echo.
echo ========================================
echo Applications are starting...
echo ========================================
echo.
echo App A: http://appa.tbk.com/ or http://localhost:10000/
echo App B: http://appb.tbk.com/ or http://localhost:20000/
echo.
echo Wait 30-60 seconds for the applications to fully start.
echo.
pause
