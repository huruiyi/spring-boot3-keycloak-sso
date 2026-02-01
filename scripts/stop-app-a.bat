@echo off
echo ========================================
echo Stopping App A (Port 10000)...
echo ========================================
echo.

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :10000') do (
    echo Found process %%a on port 10000, stopping...
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo Failed to stop process %%a
    ) else (
        echo App A stopped successfully
    )
)

echo.
echo App A stopped
echo.
timeout /t 2 /nobreak >nul
