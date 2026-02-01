# 修复 "Invalid redirect uri" 错误

## 错误信息
```
Invalid redirect uri post_logout_redirect_uri=http://appb.tbk.com
```

## 问题原因

OAuth2 和 OpenID Connect 对 URI 的匹配非常严格：
- `http://appb.tbk.com` （没有尾随斜杠）
- `http://appb.tbk.com/` （有尾随斜杠）

这两个被视为**完全不同的 URI**！

## 解决方案

### 方案 1：在 Keycloak 中添加两种格式（推荐）

在 Keycloak Admin Console 中：

#### app-a 客户端配置

1. 进入 `Clients` → `app-a` → `Settings`
2. 在 **Valid post logout redirect URIs** 中添加：
```
http://appa.tbk.com
http://appa.tbk.com/
http://localhost:10000
http://localhost:10000/
```

#### app-b 客户端配置

1. 进入 `Clients` → `app-b` → `Settings`
2. 在 **Valid post logout redirect URIs** 中添加：
```
http://appb.tbk.com
http://appb.tbk.com/
http://localhost:20000
http://localhost:20000/
```

### 方案 2：修改应用配置（已完成）

我已经修改了应用配置，明确添加尾随斜杠：

**app-a/src/main/resources/application.yml**:
```yaml
post-logout-redirect-uri: "{baseUrl}/"
```

**app-b/src/main/resources/application.yml**:
```yaml
post-logout-redirect-uri: "{baseUrl}/"
```

## 为什么会出现这个问题？

1. **Spring Security 的行为**：`{baseUrl}` 占位符可能解析为不带尾随斜杠的 URL
2. **浏览器行为**：不同浏览器对 URL 的处理可能不同
3. **Keycloak 严格匹配**：Keycloak 严格按照配置的 URI 进行匹配

## 最佳实践

### 1. 总是配置两种格式

在 Keycloak 中同时配置带和不带尾随斜杠的 URI：
```
http://example.com
http://example.com/
```

### 2. 使用明确的 URI

在应用配置中使用明确的 URI，而不是依赖占位符的默认行为：
```yaml
post-logout-redirect-uri: "http://appa.tbk.com/"
```

### 3. 测试所有场景

测试不同的访问方式：
- 通过域名访问：`http://appa.tbk.com`
- 通过 localhost 访问：`http://localhost:10000`
- 带和不带尾随斜杠

## 验证修复

### 1. 重启应用
```bash
restart-all.bat
```

### 2. 清除浏览器缓存
按 `Ctrl + Shift + Delete` 清除所有 Cookie

### 3. 测试登出
1. 登录到任一应用
2. 点击"退出登录"
3. 应该成功重定向到首页，不再出现 "Invalid redirect uri" 错误

## 常见的 URI 匹配问题

### 1. 协议不匹配
- 配置：`https://example.com`
- 实际：`http://example.com`
- 结果：❌ 失败

### 2. 端口不匹配
- 配置：`http://localhost:8080`
- 实际：`http://localhost:3000`
- 结果：❌ 失败

### 3. 路径不匹配
- 配置：`http://example.com/app`
- 实际：`http://example.com/app/`
- 结果：❌ 失败

### 4. 查询参数
- 配置：`http://example.com/callback`
- 实际：`http://example.com/callback?state=123`
- 结果：✅ 成功（查询参数会被忽略）

## 调试技巧

### 1. 查看浏览器网络标签

在浏览器开发者工具的网络标签中，查看实际发送的 redirect URI：
```
POST /auth/realms/fairy.vip/protocol/openid-connect/logout
post_logout_redirect_uri=http://appb.tbk.com/
```

### 2. 查看 Keycloak 日志

在 Keycloak 的事件日志中查看详细的错误信息。

### 3. 使用 curl 测试

```bash
curl -v "http://localhost:8080/realms/fairy.vip/protocol/openid-connect/logout" \
  -d "post_logout_redirect_uri=http://appb.tbk.com/" \
  -d "id_token_hint=..."
```

## 总结

URI 匹配问题是 OAuth2 应用中最常见的配置错误之一。解决方法很简单：

1. ✅ 在 Keycloak 中配置所有可能的 URI 变体
2. ✅ 在应用中使用明确的 URI 配置
3. ✅ 充分测试所有访问场景

现在你的配置应该可以正常工作了！