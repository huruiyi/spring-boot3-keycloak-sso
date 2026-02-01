# Keycloak 客户端配置指南

为了实现单点登录和单点登出功能，需要在 Keycloak 中配置以下两个客户端。

## 配置步骤

### 1. 访问 Keycloak Admin Console

打开浏览器访问：https://keycloak.fairy.vip/admin

### 2. 选择 Realm

选择 `spring-boot-samples-v3` realm

### 3. 创建 App A 客户端

1. 点击左侧菜单 `Clients` → `Create client`

2. 基本设置：
   - **Client ID**: `app-a`
   - **Client type**: `OpenID Connect`
   - 点击 `Next`

3. Capability config：
   - **Client authentication**: ON
   - **Authorization**: OFF
   - 点击 `Next`

4. Login settings：
   - **Valid redirect URIs**:
     ```
     http://appa.tbk.com/login/oauth2/code/keycloak
     http://localhost:10000/login/oauth2/code/keycloak
     ```
   - **Valid post logout redirect URIs**:
     ```
     http://appa.tbk.com/
     http://localhost:10000/
     ```
   - **Web origins**:
     ```
     http://appa.tbk.com
     http://localhost:10000
     ```
   - 点击 `Save`

5. 获取凭证：
   - 点击 `Credentials` 标签
   - 复制 `Client secret`

### 4. 创建 App B 客户端

重复上述步骤，创建 `app-b` 客户端：

1. 点击左侧菜单 `Clients` → `Create client`

2. 基本设置：
   - **Client ID**: `app-b`
   - **Client type**: `OpenID Connect`
   - 点击 `Next`

3. Capability config：
   - **Client authentication**: ON
   - **Authorization**: OFF
   - 点击 `Next`

4. Login settings：
   - **Valid redirect URIs**:
     ```
     http://appb.tbk.com/login/oauth2/code/keycloak
     http://localhost:20000/login/oauth2/code/keycloak
     ```
   - **Valid post logout redirect URIs**:
     ```
     http://appb.tbk.com/
     http://localhost:20000/
     ```
   - **Web origins**:
     ```
     http://appb.tbk.com
     http://localhost:20000
     ```
   - 点击 `Save`

5. 获取凭证：
   - 点击 `Credentials` 标签
   - 复制 `Client secret`

### 5. 更新应用配置

将获取的两个客户端密钥更新到应用配置文件中：

**App A 配置** (`app-a/src/main/resources/application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-a
            client-secret: <从Keycloak复制的app-a客户端密钥>
```

**App B 配置** (`app-b/src/main/resources/application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-b
            client-secret: <从Keycloak复制的app-b客户端密钥>
```

## 重要配置说明

### 为什么使用独立的客户端？

1. **更清晰的隔离**: 每个应用有独立的客户端ID，更容易追踪和管理
2. **正确的单点登出**: 独立的客户端配合正确的logout配置可以实现真正的单点登出
3. **更好的安全性**: 可以为不同应用配置不同的权限和设置

### 单点登录流程

1. 用户访问 App A，被重定向到 Keycloak
2. 在 Keycloak 登录
3. Keycloak 创建全局会话
4. 用户访问 App B，Keycloak 检测到已有会话，直接授权

### 单点登出流程

1. 用户在 App A 点击退出
2. App A 调用 Keycloak logout endpoint
3. Keycloak 清除全局会话
4. 用户再访问任何应用都需要重新登录

## 常见问题

### Q: 为什么不使用同一个客户端？

A: 虽然技术上可以使用同一个客户端，但独立客户端提供了：
- 更好的应用隔离
- 更清晰的权限管理
- 更容易实现真正的单点登出

### Q: 单点登出不工作？

A: 确保：
1. Valid post logout redirect URIs 已正确配置
2. 两个应用都配置了相同的 realm
3. 两个应用使用相同的 issuer-uri

### Q: 如何验证配置是否正确？

A:
1. 访问 http://appa.tbk.com/
2. 点击登录，输入凭证
3. 访问 http://appb.tbk.com/ - 应该直接进入，无需重新登录
4. 在任一应用退出
5. 访问另一个应用 - 应该要求重新登录

## 测试步骤

1. 启动两个应用
2. 清除浏览器所有 Cookie
3. 访问 App A，登录
4. 访问 App B - 应该直接进入
5. 在 App A 退出登录
6. 访问 App B - 应该需要重新登录
