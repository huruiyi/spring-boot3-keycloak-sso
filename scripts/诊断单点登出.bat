@echo off
echo ========================================
echo 诊断单点登出问题
echo ========================================
echo.

echo 1. 测试 App A Backchannel logout 端点...
curl -X POST http://localhost:10000/logout/connect/back-channel/keycloak -d "logout_token=test_token" -H "Content-Type: application/x-www-form-urlencoded"
echo.
echo.

echo 2. 测试 App B Backchannel logout 端点...
curl -X POST http://localhost:20000/logout/connect/back-channel/keycloak -d "logout_token=test_token" -H "Content-Type: application/x-www-form-urlencoded"
echo.
echo.

echo 3. 检查应用是否运行...
echo 检查 App A (端口 10000):
curl -s -o nul -w "HTTP状态码: %%{http_code}\n" http://localhost:10000/
echo.

echo 检查 App B (端口 20000):
curl -s -o nul -w "HTTP状态码: %%{http_code}\n" http://localhost:20000/
echo.

echo ========================================
echo 诊断完成
echo.
echo 如果看到 "Logout successful" 响应，说明端点正常
echo 如果看到 HTTP 200 状态码，说明应用正在运行
echo ========================================
pause