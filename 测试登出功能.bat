@echo off
echo ========================================
echo 测试登出功能
echo ========================================
echo.

echo 1. 检查应用是否运行...
curl -s -o nul -w "App A HTTP状态码: %%{http_code}\n" http://localhost:10000/
curl -s -o nul -w "App B HTTP状态码: %%{http_code}\n" http://localhost:20000/
echo.

echo 2. 检查会话状态端点...
echo App A 会话状态:
curl -s http://localhost:10000/api/session/status
echo.
echo.

echo App B 会话状态:
curl -s http://localhost:20000/api/session/status
echo.
echo.

echo 3. 测试 Backchannel logout 端点...
echo 测试 App A:
curl -X POST http://localhost:10000/logout/connect/back-channel/keycloak -d "logout_token=test"
echo.
echo.

echo 测试 App B:
curl -X POST http://localhost:20000/logout/connect/back-channel/keycloak -d "logout_token=test"
echo.
echo.

echo ========================================
echo 测试完成
echo.
echo 接下来请在浏览器中测试:
echo 1. 访问 http://localhost:10000/ 并登录
echo 2. 访问 http://localhost:20000/ (应该自动登录)
echo 3. 在任一应用点击"退出登录"按钮
echo 4. 检查是否真正退出登录
echo ========================================
pause