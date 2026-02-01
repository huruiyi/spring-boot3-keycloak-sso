@echo off
echo ========================================
echo Stopping App B (Port 20000)...
echo ========================================
echo.

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :20000') do (
    echo Found process %%a on port 20000, stopping...
    taskkill /F /PID %%a >nul 2>&1
    if errorlevel 1 (
        echo Failed to stop process %%a
    ) else (
        echo App B stopped successfully
    )
)

echo.
echo App B stopped
echo.
timeout /t 2 /nobreak >nul
