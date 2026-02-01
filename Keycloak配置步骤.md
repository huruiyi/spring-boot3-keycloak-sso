# Keycloak 配置步骤 - 实现真正的单点登出

## 前提条件

- 访问 Keycloak Admin Console: https://keycloak.fairy.vip/admin
- 管理员权限
- Realm: `spring-boot-samples-v3`

---

## 步骤 1: 创建 app-a 客户端

### 1.1 创建客户端

1. 登录 Keycloak Admin Console
2. 选择 `spring-boot-samples-v3` realm
3. 点击左侧菜单 `Clients`
4. 点击 `Create client` 按钮

### 1.2 General Settings

- **Client type**: `OpenID Connect`
- **Client ID**: `app-a`
- 点击 `Next`

### 1.3 Capability config

- **Client authentication**: `ON` ✅
- **Authorization**: `OFF`
- **Authentication flow**:
  - ✅ Standard flow
  - ✅ Direct access grants
- 点击 `Next`

### 1.4 Login settings

**Root URL**: `http://appa.tbk.com`

**Home URL**: `http://appa.tbk.com`

**Valid redirect URIs**:
```
http://appa.tbk.com/login/oauth2/code/keycloak
http://localhost:10000/login/oauth2/code/keycloak
```

**Valid post logout redirect URIs**:
```
http://appa.tbk.com
http://appa.tbk.com/
http://localhost:10000
http://localhost:10000/
```

**Web origins**:
```
http://appa.tbk.com
http://localhost:10000
```

点击 `Save`

### 1.5 配置 Backchannel Logout

1. 在 `app-a` 客户端页面，点击 `Advanced` 标签
2. 滚动到 **Logout settings** 部分
3. 配置以下选项：

**Backchannel logout URL**:
```
http://appa.tbk.com/logout/connect/back-channel/keycloak
```

**Backchannel logout session required**: `ON` ✅

**Backchannel logout revoke offline sessions**: `ON` ✅

4. 点击 `Save`

### 1.6 获取客户端密钥

1. 点击 `Credentials` 标签
2. 复制 `Client secret`
3. 如果密钥不是 `K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg`，需要更新 `app-a/src/main/resources/application.yml` 中的配置

---

## 步骤 2: 创建 app-b 客户端

### 2.1 创建客户端

1. 点击左侧菜单 `Clients`
2. 点击 `Create client` 按钮

### 2.2 General Settings

- **Client type**: `OpenID Connect`
- **Client ID**: `app-b`
- 点击 `Next`

### 2.3 Capability config

- **Client authentication**: `ON` ✅
- **Authorization**: `OFF`
- **Authentication flow**:
  - ✅ Standard flow
  - ✅ Direct access grants
- 点击 `Next`

### 2.4 Login settings

**Root URL**: `http://appb.tbk.com`

**Home URL**: `http://appb.tbk.com`

**Valid redirect URIs**:
```
http://appb.tbk.com/login/oauth2/code/keycloak
http://localhost:20000/login/oauth2/code/keycloak
```

**Valid post logout redirect URIs**:
```
http://appb.tbk.com
http://appb.tbk.com/
http://localhost:20000
http://localhost:20000/
```

**Web origins**:
```
http://appb.tbk.com
http://localhost:20000
```

点击 `Save`

### 2.5 配置 Backchannel Logout

1. 在 `app-b` 客户端页面，点击 `Advanced` 标签
2. 滚动到 **Logout settings** 部分
3. 配置以下选项：

**Backchannel logout URL**:
```
http://appb.tbk.com/logout/connect/back-channel/keycloak
```

**Backchannel logout session required**: `ON` ✅

**Backchannel logout revoke offline sessions**: `ON` ✅

4. 点击 `Save`

### 2.6 获取客户端密钥

1. 点击 `Credentials` 标签
2. 复制 `Client secret`
3. 如果密钥不是 `K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg`，需要更新 `app-b/src/main/resources/application.yml` 中的配置

---

## 步骤 3: 验证配置

### 3.1 检查客户端列表

在 `Clients` 页面，应该看到：
- ✅ `app-a` (OpenID Connect)
- ✅ `app-b` (OpenID Connect)

### 3.2 验证 Backchannel Logout URL

确保两个客户端的 Backchannel logout URL 都已正确配置：
- `app-a`: `http://appa.tbk.com/logout/connect/back-channel/keycloak`
- `app-b`: `http://appb.tbk.com/logout/connect/back-channel/keycloak`

---

## 步骤 4: 测试单点登出

### 4.1 启动应用

```bash
# 重启两个应用
restart-all.bat
```

### 4.2 测试流程

1. **清除浏览器 Cookie**
   - 按 `Ctrl + Shift + Delete`
   - 清除所有 Cookie

2. **访问 App A**
   - 打开浏览器访问: http://appa.tbk.com/
   - 点击 "登录"
   - 输入 Keycloak 凭证
   - 应该成功登录到 App A ✅

3. **访问 App B**
   - 在同一浏览器中访问: http://appb.tbk.com/
   - 应该自动登录，无需输入密码 ✅

4. **在 App A 退出登录**
   - 回到 App A 页面
   - 点击 "退出登录"
   - 应该被重定向到 App A 首页 ✅

5. **验证 App B 已登出**
   - 刷新 App B 页面
   - 应该显示登录页面，需要重新登录 ✅

6. **反向测试**
   - 重新登录到两个应用
   - 在 App B 退出登录
   - 刷新 App A 页面
   - 应该显示登录页面 ✅

---

## 故障排查

### 问题 1: 重定向循环

**症状**: 访问应用时出现 "重定向次数过多" 错误

**原因**: Keycloak 中没有创建对应的客户端

**解决方案**:
1. 确认已在 Keycloak 中创建 `app-a` 和 `app-b` 客户端
2. 确认 `Valid redirect URIs` 配置正确
3. 重启应用

### 问题 2: 单点登出不工作

**症状**: 在 App A 登出后，App B 仍然保持登录状态

**可能原因**:

1. **Backchannel logout URL 配置不正确**
   - 检查 URL 是否正确: `http://appa.tbk.com/logout/connect/back-channel/keycloak`
   - 确认没有多余的空格或字符

2. **Keycloak 无法访问应用**
   - Keycloak 服务器必须能够访问应用的 Backchannel logout URL
   - 如果 Keycloak 在云端，应用在本地，需要配置反向代理或使用公网 IP

3. **Backchannel logout session required 未开启**
   - 在 Keycloak 客户端设置中，确认 `Backchannel logout session required` 为 `ON`

4. **应用未收到 Backchannel logout 请求**
   - 查看应用控制台日志，应该看到 "收到 Backchannel logout 请求"
   - 如果没有看到，说明 Keycloak 没有发送请求

**调试步骤**:

1. **测试 Backchannel logout 端点是否可访问**
   ```bash
   # 运行测试脚本
   test-backchannel-logout.bat
   ```
   应该看到 "Logout successful" 响应

2. **检查 Keycloak 日志**
   - 在 Keycloak Admin Console 中查看事件日志
   - 查找 LOGOUT 事件
   - 检查是否有 Backchannel logout 请求失败的记录

3. **检查应用日志**
   - 在 App A 登出后，查看 App B 的控制台
   - 应该看到 "收到 Backchannel logout 请求"
   - 如果没有，说明 Keycloak 没有发送请求到 App B

4. **验证网络连通性**
   ```bash
   # 从 Keycloak 服务器测试
   curl -X POST http://appa.tbk.com/logout/connect/back-channel/keycloak
   curl -X POST http://appb.tbk.com/logout/connect/back-channel/keycloak
   ```

**临时解决方案**:

如果 Backchannel logout 无法工作，可以使用以下替代方案：

1. **使用较短的会话超时**
   - 在 application.yml 中设置 `spring.session.timeout: 5m`
   - 会话会在 5 分钟后自动过期

2. **使用前端轮询**
   - 在页面上添加 JavaScript 定期检查会话状态
   - 检测到会话失效时自动刷新页面

3. **提示用户手动刷新**
   - 在页面上添加提示："退出登录后，请刷新其他应用页面"

### 问题 3: 客户端密钥不匹配

**症状**: 登录时出现 "Invalid client credentials" 错误

**原因**: application.yml 中的 client-secret 与 Keycloak 中的不一致

**解决方案**:
1. 在 Keycloak 中查看客户端的 `Credentials` 标签
2. 复制正确的 `Client secret`
3. 更新 application.yml 中的 `client-secret`
4. 重启应用

### 问题 4: Backchannel logout 请求失败

**症状**: 应用日志中看到 Backchannel logout 请求失败

**原因**: 
- Keycloak 无法访问应用的 Backchannel logout URL
- 网络防火墙阻止了请求
- 应用未正确处理 Backchannel logout 请求

**解决方案**:
1. 确认 Keycloak 服务器可以访问应用 URL
2. 如果应用在内网，考虑使用反向代理
3. 检查防火墙设置
4. 查看应用日志中的详细错误信息

---

## 配置检查清单

在测试之前，请确认以下所有项目：

### Keycloak 配置

- [ ] 已创建 `app-a` 客户端
- [ ] 已创建 `app-b` 客户端
- [ ] 两个客户端的 `Client authentication` 都是 `ON`
- [ ] 配置了正确的 `Valid redirect URIs`
- [ ] 配置了正确的 `Valid post logout redirect URIs`
- [ ] 配置了正确的 `Web origins`
- [ ] 配置了 `Backchannel logout URL`
- [ ] `Backchannel logout session required` 已开启
- [ ] `Backchannel logout revoke offline sessions` 已开启

### 应用配置

- [ ] App A 使用 `client-id: app-a`
- [ ] App B 使用 `client-id: app-b`
- [ ] 两个应用的 `client-secret` 与 Keycloak 中的一致
- [ ] 两个应用的 `issuer-uri` 正确
- [ ] 两个应用已重启

### 网络配置

- [ ] hosts 文件已配置 `appa.tbk.com` 和 `appb.tbk.com`
- [ ] Nginx 已启动并正确配置
- [ ] Keycloak 服务器可以访问应用的 Backchannel logout URL

---

## 工作原理

### 单点登录流程

```
用户访问 App A
    ↓
重定向到 Keycloak 登录页面
    ↓
用户输入凭证
    ↓
Keycloak 创建全局会话
    ↓
重定向回 App A，携带授权码
    ↓
App A 用授权码换取 token
    ↓
App A 创建本地会话
    ↓
用户访问 App B
    ↓
重定向到 Keycloak
    ↓
Keycloak 检测到已有会话，直接授权
    ↓
重定向回 App B，携带授权码
    ↓
App B 用授权码换取 token
    ↓
App B 创建本地会话
```

### 单点登出流程（Backchannel Logout）

```
用户在 App A 点击退出
    ↓
App A 调用 Keycloak logout endpoint
    ↓
Keycloak 清除全局会话
    ↓
Keycloak 查找该用户的所有活动客户端会话
    ↓
Keycloak 向每个客户端的 Backchannel logout URL 发送 POST 请求
    ├─→ POST http://appa.tbk.com/logout/connect/back-channel/keycloak
    └─→ POST http://appb.tbk.com/logout/connect/back-channel/keycloak
    ↓
App A 和 App B 收到请求，清除本地会话
    ↓
Keycloak 重定向用户到 post_logout_redirect_uri
    ↓
用户访问任何应用都需要重新登录
```

---

## 行业最佳实践

1. **使用独立的客户端 ID**
   - 每个应用有独立的客户端
   - 便于管理和审计
   - 支持细粒度的权限控制

2. **启用 Backchannel Logout**
   - 实现真正的单点登出
   - 提升用户体验
   - 增强安全性

3. **配置合理的会话超时**
   - 默认 30 分钟
   - 根据业务需求调整

4. **使用 HTTPS**
   - 生产环境必须使用 HTTPS
   - 保护 token 和会话安全

5. **监控和日志**
   - 启用详细的安全日志
   - 监控登录/登出事件
   - 及时发现异常行为

---

## 参考资料

- [Keycloak Documentation - OIDC Logout](https://www.keycloak.org/docs/latest/securing_apps/#logout)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [OpenID Connect Backchannel Logout](https://openid.net/specs/openid-connect-backchannel-1_0.html)
