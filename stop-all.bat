@echo off
echo ========================================
echo Stopping SSO Demo Applications...
echo ========================================
echo.

echo [1/2] Stopping App A (Port 10000)...
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
echo [2/2] Stopping App B (Port 20000)...
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
echo ========================================
echo All applications stopped
echo ========================================
echo.
timeout /t 2 /nobreak >nul
