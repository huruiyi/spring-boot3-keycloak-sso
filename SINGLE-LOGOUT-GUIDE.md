# 单点登出（Single Logout）配置指南

## 问题说明

使用同一个 client-id 时，Keycloak 的 Backchannel logout URL 只能配置一个，无法同时通知多个应用。要实现真正的单点登出，需要为每个应用创建独立的 Keycloak 客户端。

## 解决方案：使用独立的客户端（推荐）

### 步骤 1：在 Keycloak 中创建两个客户端

#### 创建 app-a 客户端

1. 访问 Keycloak Admin Console: https://keycloak.fairy.vip/admin
2. 选择 `spring-boot-samples-v3` realm
3. 点击 `Clients` → `Create client`

**基本设置：**
- **Client ID**: `app-a`
- **Client type**: `OpenID Connect`
- 点击 `Next`

**Capability config：**
- **Client authentication**: ON
- **Authorization**: OFF
- 点击 `Next`

**Login settings：**
- **Valid redirect URIs**:
  ```
  http://appa.tbk.com/login/oauth2/code/keycloak
  http://localhost:10000/login/oauth2/code/keycloak
  ```
- **Valid post logout redirect URIs**:
  ```
  http://appa.tbk.com/*
  http://localhost:10000/*
  ```
- **Web origins**:
  ```
  http://appa.tbk.com
  http://localhost:10000
  ```
- 点击 `Save`

**配置 Backchannel Logout：**
1. 在 `app-a` 客户端页面，找到 `Advanced` 标签
2. 滚动到 `Logout settings`
3. 配置：
   - **Backchannel logout URL**: `http://appa.tbk.com/logout/connect/back-channel/keycloak`
   - **Backchannel logout session required**: ON
   - **Backchannel logout revoke offline sessions**: ON
4. 点击 `Save`

**获取凭证：**
- 点击 `Credentials` 标签
- 复制 `Client secret`（如果和 management-api 一样可以不用改）

#### 创建 app-b 客户端

重复上述步骤，创建 `app-b` 客户端：

**Login settings：**
- **Valid redirect URIs**:
  ```
  http://appb.tbk.com/login/oauth2/code/keycloak
  http://localhost:20000/login/oauth2/code/keycloak
  ```
- **Valid post logout redirect URIs**:
  ```
  http://appb.tbk.com/*
  http://localhost:20000/*
  ```
- **Web origins**:
  ```
  http://appb.tbk.com
  http://localhost:20000
  ```

**配置 Backchannel Logout：**
- **Backchannel logout URL**: `http://appb.tbk.com/logout/connect/back-channel/keycloak`
- **Backchannel logout session required**: ON
- **Backchannel logout revoke offline sessions**: ON

### 步骤 2：应用配置已更新

应用配置已经更新为使用独立的 client-id：

**App A** (`app-a/src/main/resources/application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-a
            client-secret: K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg
```

**App B** (`app-b/src/main/resources/application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-b
            client-secret: K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg
```

## 单点登出工作原理

### 使用独立客户端 + Backchannel Logout 的流程：

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
    ↓
App A 和 App B 都收到通知并清除本地会话
    ↓
用户访问任何应用都需要重新登录
```

### 为什么需要独立的客户端？

1. **独立的 Backchannel logout URL**: 每个客户端可以配置自己的回调 URL
2. **会话追踪**: Keycloak 可以追踪每个客户端的会话状态
3. **精确通知**: 登出时 Keycloak 知道要通知哪些应用
4. **更好的隔离**: 每个应用有独立的配置和权限管理

## 验证单点登出

### 测试步骤：

1. **重启两个应用**
   ```bash
   restart-all.bat
   ```

2. **清除浏览器 Cookie**

3. **访问 App A**: http://appa.tbk.com/
   - 点击登录
   - 输入 Keycloak 凭证

4. **访问 App B**: http://appb.tbk.com/
   - 应该直接进入，无需重新登录 ✅

5. **在 App A 退出登录**

6. **刷新 App B 页面**: http://appb.tbk.com/
   - 应该显示登录页面，需要重新登录 ✅

7. **在 App B 登录**

8. **访问 App A**: http://appa.tbk.com/
   - 应该直接进入，无需重新登录 ✅

### 如果单点登出不工作，检查：

1. ✅ 两个应用使用不同的 `client-id`（app-a 和 app-b）
2. ✅ 两个应用使用相同的 `issuer-uri`
3. ✅ Keycloak 中为每个客户端配置了 Backchannel logout URL
4. ✅ Keycloak 中配置了所有需要的 redirect URIs
5. ✅ Keycloak 中配置了所有需要的 post logout redirect URIs
6. ✅ 两个应用都已重启
7. ✅ 检查应用日志，确认收到了 Backchannel logout 请求

## 调试技巧

### 查看应用日志

应用配置了 DEBUG 级别的日志，可以看到：

```
# 登录时
o.s.security.oauth2 : Authenticated user: [username]

# 收到 Backchannel logout 时
o.s.security.oauth2 : Processing backchannel logout request
o.s.security.web.authentication.logout : Invalidating session
```

### 检查 Keycloak 日志

在 Keycloak Admin Console 中：
1. 进入 `Clients` → `app-a` 或 `app-b`
2. 点击 `Sessions` 标签
3. 可以看到当前活动的会话

### 测试 Backchannel logout URL

可以手动测试 Backchannel logout URL 是否可访问：

```bash
# 测试 App A
curl -X POST http://appa.tbk.com/logout/connect/back-channel/keycloak

# 测试 App B
curl -X POST http://appb.tbk.com/logout/connect/back-channel/keycloak
```

## 常见问题

### Q: 为什么不能使用同一个 client-id？

A: Keycloak 的 Backchannel logout URL 只能配置一个。使用同一个 client-id 时，Keycloak 无法同时通知多个应用。

### Q: 独立客户端会影响单点登录吗？

A: 不会。单点登录（SSO）是基于 Keycloak 的全局会话，与客户端数量无关。只要用户在 Keycloak 有活动会话，访问任何配置了该 realm 的应用都会自动登录。

### Q: Backchannel logout 和 Frontchannel logout 有什么区别？

A: 
- **Backchannel logout**: Keycloak 服务器直接向应用服务器发送 HTTP POST 请求，更可靠
- **Frontchannel logout**: 通过浏览器重定向通知应用，依赖浏览器和网络状态

推荐使用 Backchannel logout，因为它更可靠。

### Q: 如果应用在内网，Keycloak 无法访问怎么办？

A: 如果 Keycloak 无法直接访问应用的 Backchannel logout URL（例如应用在内网），可以考虑：
1. 使用 Frontchannel logout（不太可靠）
2. 配置反向代理，让 Keycloak 可以访问应用
3. 使用较短的会话超时时间，让会话自动过期

## 当前配置状态

✅ App A: 使用 `client-id: app-a`
✅ App B: 使用 `client-id: app-b`
✅ 两个应用使用独立的客户端
✅ 配置了动态 redirect-uri
✅ 配置了动态 post-logout-redirect-uri
✅ 配置了会话管理
✅ 配置了 Cookie 清除

## 下一步

1. 在 Keycloak 中创建 `app-a` 和 `app-b` 两个客户端
2. 为每个客户端配置 Backchannel logout URL
3. 重启两个应用
4. 按照上述验证步骤测试单点登出功能

## 备注

如果你的 Keycloak 客户端密钥不同，记得更新 `application.yml` 中的 `client-secret`。
