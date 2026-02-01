@echo off
title App B - Port 20000
echo ========================================
echo Starting App B (Port 20000)...
echo ========================================
cd app-b
start cmd /k "title App B - Spring Boot & mvn spring-boot:run"
echo App B is starting...
echo You can access it at: http://appb.tbk.com/ or http://localhost:20000/
pause
