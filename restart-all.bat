@echo off
echo ========================================
echo Restarting SSO Demo Applications...
echo ========================================
echo.

echo Step 1: Stopping all applications...
call stop-all.bat

echo.
echo Step 2: Waiting for ports to be released...
timeout /t 3 /nobreak >nul

echo.
echo Step 3: Starting applications...
call start-all.bat
