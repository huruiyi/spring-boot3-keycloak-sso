@echo off
echo ========================================
echo 单点登出测试指南
echo ========================================
echo.
echo [步骤1] 清除浏览器Cookie
echo - 打开浏览器（推荐使用Chrome或Firefox）
echo - 按F12打开开发者工具
echo - 进入Application/应用程序标签
echo - 找到Cookies
echo - 删除所有域名（appa.tbk.com, appb.tbk.com, keycloak.fairy.vip）
echo - 或者使用无痕模式：Ctrl+Shift+N
echo.
pause
echo.
echo [步骤2] 访问App A
echo - 打开浏览器访问: http://appa.tbk.com/
echo - 点击"通过Keycloak登录"
echo - 输入用户名和密码
echo.
pause
echo.
echo [步骤3] 测试单点登录
echo - 在新标签页访问: http://appb.tbk.com/
echo - 应该直接进入home页面，无需重新登录
echo - 如果需要重新登录，说明单点登录有问题
echo.
pause
echo.
echo [步骤4] 测试单点登出 - 重要步骤
echo - 回到App A标签页
echo - 点击右上角"退出登录"
echo - 看到"正在注销登录..."页面
echo - 自动跳转回App A首页
echo.
pause
echo.
echo [步骤5] 验证单点登出
echo - 回到App B标签页
echo - 刷新页面（F5）
echo.
echo.
echo ========================================
echo 预期结果:
echo ========================================
echo.
echo 如果单点登出正常工作：
echo - App B应该显示登录页面（不是home页面）
echo - 说明Keycloak会话已清除
echo - 需要重新登录才能访问任一应用
echo.
echo 如果单点登出不工作：
echo - App B仍然显示home页面（已登录状态）
echo - 说明App A退出不影响App B
echo - 需要检查配置
echo.
pause
echo.
echo [步骤6] 重新登录测试
echo - 在App B登录
echo - 访问App A
echo - 应该直接进入（无需重新登录）
echo.
pause
echo.
echo ========================================
echo 测试完成
echo ========================================
echo.
echo 如果测试通过，单点登录和单点登出都正常工作！
echo 如果测试失败，请查看 SINGLE-LOGOUT-GUIDE.md 获取详细说明
echo.
pause
