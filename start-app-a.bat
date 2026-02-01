@echo off
title App A - Port 10000
echo ========================================
echo Starting App A (Port 10000)...
echo ========================================
cd app-a
start cmd /k "title App A - Spring Boot & mvn spring-boot:run"
echo App A is starting...
echo You can access it at: http://appa.tbk.com/ or http://localhost:10000/
pause
