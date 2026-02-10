# Keycloak Admin API 配置指南

## 问题描述

注册功能报错：`jakarta.ws.rs.NotAuthorizedException: HTTP 401 Unauthorized`

这表示应用无法通过Keycloak Admin API创建用户，原因是认证失败。

---

## 解决方案

### 方案1: 使用Master Realm的Admin用户（推荐用于开发环境）

#### 步骤1: 验证Admin密码

1. 访问Keycloak Admin Console: http://localhost:8080
2. 使用admin账户登录
3. 如果无法登录，需要重置admin密码

#### 步骤2: 确认Admin密码

当前配置使用的密码是：`admin`

如果Keycloak的admin密码不是`admin`，需要更新`application.yml`：

```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: fairy.vip
  admin-client-id: admin-cli
  admin-username: admin
  admin-password: 你的实际密码  # 修改这里
```

#### 步骤3: 重启应用

```bash
.\scripts\restart-all.bat
```

---

### 方案2: 创建Service Account客户端（推荐用于生产环境）

这个方案更安全，不需要使用master realm的admin账户。

#### 步骤1: 在Keycloak中创建Service Account客户端

1. 登录Keycloak Admin Console: http://localhost:8080
2. 选择realm: `fairy.vip`
3. 进入 `Clients` 页面
4. 点击 `Create client` 按钮

**客户端配置**:
```
Client ID: user-management-service
Client Protocol: openid-connect
```

点击 `Next`

**Capability config**:
```
☑ Client authentication: ON
☑ Service accounts roles: ON
☐ Standard flow: OFF
☐ Direct access grants: OFF
```

点击 `Next`，然后 `Save`

#### 步骤2: 获取Client Secret

1. 在刚创建的客户端页面
2. 进入 `Credentials` 标签页
3. 复制 `Client secret` 的值

#### 步骤3: 配置Service Account权限

1. 在客户端页面，进入 `Service account roles` 标签页
2. 点击 `Assign role` 按钮
3. 选择 `Filter by clients`
4. 搜索并添加以下角色：
   - `realm-management` → `manage-users`
   - `realm-management` → `view-users`
   - `realm-management` → `query-users`

#### 步骤4: 更新application.yml

```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: fairy.vip
  admin-client-id: user-management-service  # 改为新创建的客户端ID
  admin-username: user-management-service   # Service Account使用client-id作为username
  admin-password: 你复制的client-secret      # 改为client secret
```

#### 步骤5: 更新KeycloakService.java

需要修改认证方式，使用client credentials grant：

```java
private Keycloak getKeycloakInstance() {
    return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)  // 改为使用目标realm而不是master
            .grantType("client_credentials")  // 添加这行
            .clientId(adminClientId)
            .clientSecret(adminPassword)  // 使用client secret
            .build();
}
```

#### 步骤6: 重启应用

```bash
.\scripts\restart-all.bat
```

---

## 快速诊断

### 测试Keycloak Admin API连接

创建一个测试端点来验证连接：

```java
@GetMapping("/test-keycloak-connection")
public ResponseEntity<String> testKeycloakConnection() {
    try {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        int userCount = realmResource.users().count();
        return ResponseEntity.ok("连接成功！当前用户数: " + userCount);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("连接失败: " + e.getMessage());
    }
}
```

访问: http://localhost:10000/test-keycloak-connection

---

## 常见问题

### Q1: 如何重置Keycloak admin密码？

**方法1: 通过Docker容器**
```bash
docker exec -it keycloak /opt/keycloak/bin/kc.sh set-password --username admin --new-password admin
```

**方法2: 通过环境变量**
```bash
# 停止Keycloak
# 设置环境变量
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=admin
# 重启Keycloak
```

### Q2: 401错误的其他可能原因

1. **Keycloak服务未运行**
   ```bash
   curl http://localhost:8080
   ```

2. **Realm名称错误**
   - 确认realm是 `fairy.vip` 而不是其他名称

3. **admin-cli客户端被禁用**
   - 在Keycloak Admin Console中检查admin-cli客户端状态

4. **网络问题**
   - 确认应用可以访问 http://localhost:8080

### Q3: 如何验证Service Account权限？

1. 登录Keycloak Admin Console
2. 进入 `Clients` → `user-management-service`
3. 进入 `Service account roles` 标签页
4. 确认已分配 `manage-users` 角色

---

## 推荐配置（开发环境）

对于开发环境，最简单的方式是使用master realm的admin账户：

**application.yml**:
```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: fairy.vip
  admin-client-id: admin-cli
  admin-username: admin
  admin-password: admin  # 确保这是正确的密码
```

**KeycloakService.java** (保持不变):
```java
private Keycloak getKeycloakInstance() {
    return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")  // 使用master realm认证
            .username(adminUsername)
            .password(adminPassword)
            .clientId(adminClientId)
            .build();
}
```

---

## 推荐配置（生产环境）

对于生产环境，使用Service Account更安全：

**application.yml**:
```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: fairy.vip
  admin-client-id: user-management-service
  admin-client-secret: 你的client-secret
```

**KeycloakService.java**:
```java
@Value("${keycloak.admin-client-secret}")
private String adminClientSecret;

private Keycloak getKeycloakInstance() {
    return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .grantType("client_credentials")
            .clientId(adminClientId)
            .clientSecret(adminClientSecret)
            .build();
}
```

---

## 验证步骤

1. ✅ 确认Keycloak正在运行
2. ✅ 确认可以登录Keycloak Admin Console
3. ✅ 确认admin密码正确
4. ✅ 确认realm名称为 `fairy.vip`
5. ✅ 更新application.yml配置
6. ✅ 重启应用
7. ✅ 测试注册功能

---

**下一步**: 请确认您的Keycloak admin密码，然后选择使用方案1或方案2。
