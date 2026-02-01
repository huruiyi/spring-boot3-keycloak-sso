@echo off
echo ========================================
echo 测试 Backchannel Logout 端点
echo ========================================
echo.

echo 测试 App A Backchannel Logout 端点...
curl -X POST http://localhost:10000/logout/connect/back-channel/keycloak -d "logout_token=test" -H "Content-Type: application/x-www-form-urlencoded"
echo.
echo.

echo 测试 App B Backchannel Logout 端点...
curl -X POST http://localhost:20000/logout/connect/back-channel/keycloak -d "logout_token=test" -H "Content-Type: application/x-www-form-urlencoded"
echo.
echo.

echo ========================================
echo 测试完成
echo ========================================
pause
