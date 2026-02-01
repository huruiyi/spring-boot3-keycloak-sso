# Keycloak 单点登录与单点登出完整实现

基于 Spring Boot 3 和 Keycloak 的单点登录（SSO）和单点登出（SLO）完整演示项目，包含两个独立的 Spring Boot 应用。

## 项目特性

✅ **单点登录（SSO）** - 一次登录，多应用访问  
✅ **单点登出（SLO）** - 一处登出，全局退出  
✅ **Backchannel Logout** - 服务器端登出通知  
✅ **会话管理** - 自动会话注册和失效处理  
✅ **调试信息** - 详细的认证状态显示  
✅ **响应式 UI** - 现代化的用户界面  

## 技术栈

- **Spring Boot**: 3.2.0
- **Java**: 17
- **Spring Security**: OAuth 2.0 / OpenID Connect
- **Thymeleaf**: 模板引擎
- **Keycloak**: 26.5.2 (身份认证服务器)
- **Maven**: 项目管理工具

## 项目结构

```
sso-demo/
├── app-a/                          # 应用 A（端口 10000）
│   ├── src/main/java/
│   │   └── com/example/appa/
│   │       ├── AppAApplication.java
│   │       ├── config/
│   │       │   ├── SecurityConfig.java              # 安全配置
│   │       │   ├── OAuth2LoginSuccessHandler.java   # 登录成功处理器
│   │       │   └── LogoutTokenJwtDecoder.java       # Logout Token 解码器
│   │       └── controller/
│   │           ├── HomeController.java              # 主页控制器
│   │           └── BackchannelLogoutController.java # Backchannel 登出处理
│   ├── src/main/resources/
│   │   ├── application.yml         # 应用配置
│   │   └── templates/              # Thymeleaf 模板
│   └── pom.xml
│
├── app-b/                          # 应用 B（端口 20000，结构同 app-a）
│
├── scripts/                    # 批处理脚本目录
│   ├── start-app-a.bat        # 启动 App A
│   ├── start-app-b.bat        # 启动 App B
│   ├── start-all.bat          # 同时启动两个应用
│   ├── stop-app-a.bat         # 停止 App A
│   ├── stop-app-b.bat         # 停止 App B
│   ├── stop-all.bat           # 停止所有应用
│   └── restart-all.bat        # 重启所有应用
│
└── pom.xml                     # 父 POM
```

## 快速开始

### 前置要求

1. **JDK 17+**
2. **Maven 3.6+**
3. **Keycloak 26.5.2** (运行在 http://localhost:8080)
4. 管理员权限（用于修改 hosts 文件）

### 1. 配置 Hosts 文件

**Windows:**
```bash
# 以管理员身份编辑 C:\Windows\System32\drivers\etc\hosts
# 添加以下内容:
127.0.0.1 appa.tbk.com
127.0.0.1 appb.tbk.com
```

**Linux/Mac:**
```bash
sudo nano /etc/hosts
# 添加以下内容:
127.0.0.1 appa.tbk.com
127.0.0.1 appb.tbk.com
```

### 2. 配置 Keycloak

详细配置步骤请参考 [Keycloak 配置指南](#keycloak-配置)

### 3. 启动应用

**方式一：使用批处理脚本（Windows）**
```bash
# 启动 App A
scripts\start-app-a.bat

# 启动 App B
scripts\start-app-b.bat

# 或同时启动两个应用
scripts\start-all.bat
```

**方式二：手动启动**
```bash
# 启动 App A
cd app-a
mvn spring-boot:run

# 启动 App B（新终端）
cd app-b
mvn spring-boot:run
```

### 4. 访问应用

- **App A**: http://appa.tbk.com/ 或 http://localhost:10000/
- **App B**: http://appb.tbk.com/ 或 http://localhost:20000/

## Keycloak 配置

### 创建 Realm

1. 访问 Keycloak Admin Console: http://localhost:8080/admin
2. 创建或选择 Realm: `fairy.vip`

### 创建客户端 App A

1. 进入 `Clients` → `Create client`

2. **基本设置**:
   - Client ID: `app-a`
   - Client type: `OpenID Connect`

3. **Capability config**:
   - Client authentication: `ON`
   - Authorization: `OFF`

4. **Login settings**:
   - Valid redirect URIs:
     ```
     http://appa.tbk.com/login/oauth2/code/keycloak
     http://localhost:10000/login/oauth2/code/keycloak
     ```
   - Valid post logout redirect URIs:
     ```
     http://appa.tbk.com/*
     http://localhost:10000/*
     ```
   - Web origins:
     ```
     http://appa.tbk.com
     http://localhost:10000
     ```

5. **Advanced → Logout settings**:
   - Backchannel logout URL: `http://appa.tbk.com/logout/connect/back-channel/keycloak`
   - Backchannel logout session required: `ON`
   - Backchannel logout revoke offline sessions: `ON`

6. **获取凭证**:
   - 进入 `Credentials` 标签
   - 复制 `Client secret`
   - 更新到 `app-a/src/main/resources/application.yml`

### 创建客户端 App B

重复上述步骤，创建 `app-b` 客户端：
- Client ID: `app-b`
- Backchannel logout URL: `http://appb.tbk.com/logout/connect/back-channel/keycloak`
- 其他配置类似，只需将 URL 中的 `appa` 改为 `appb`，端口改为 `20000`

## 核心功能实现

### 1. 单点登录（SSO）

**工作原理**:
- 两个应用共享同一个 Keycloak Realm (`fairy.vip`)
- 使用相同的 `issuer-uri`
- Keycloak 通过全局会话管理认证状态
- 用户在一个应用登录后，访问其他应用时自动完成认证

**关键配置** (`application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/fairy.vip
```

### 2. 单点登出（SLO）

**工作原理**:
- 使用 Keycloak 的 Backchannel Logout 机制
- 用户在任一应用退出时，Keycloak 向所有应用发送登出通知
- 应用收到通知后，使本地会话失效

**实现步骤**:

#### 步骤 1: 会话注册

`OAuth2LoginSuccessHandler.java` - 登录成功时注册会话到 SessionRegistry:
```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    Authentication authentication) {
    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
    String userId = oidcUser.getSubject();
    
    // 注册会话，使用 Keycloak 用户 ID 作为 principal
    sessionRegistry.registerNewSession(request.getSession().getId(), userId);
}
```

#### 步骤 2: Logout Token 解码

`LogoutTokenJwtDecoder.java` - 支持解码 Keycloak 的 `logout+jwt` 类型 token:
```java
private void customizeJwtProcessor(JWTProcessor<SecurityContext> jwtProcessor) {
    DefaultJWTProcessor<SecurityContext> processor = 
        (DefaultJWTProcessor<SecurityContext>) jwtProcessor;
    
    // 允许 JWT 和 logout+jwt 类型
    processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
        new JOSEObjectType("JWT"),
        new JOSEObjectType("logout+jwt")
    ));
}
```

#### 步骤 3: Backchannel Logout 处理

`BackchannelLogoutController.java` - 接收并处理 Keycloak 的登出通知:
```java
@PostMapping("/logout/connect/back-channel/keycloak")
public ResponseEntity<String> backchannelLogout(
        @RequestParam(value = "logout_token", required = false) String logoutToken) {
    
    // 1. 解码 logout token
    Jwt jwt = jwtDecoder.decode(logoutToken);
    String userId = jwt.getSubject();
    
    // 2. 从 SessionRegistry 获取该用户的所有会话
    List<SessionInformation> sessions = 
        sessionRegistry.getAllSessions(userId, false);
    
    // 3. 使所有会话失效
    for (SessionInformation session : sessions) {
        session.expireNow();
    }
    
    return ResponseEntity.ok("Logout successful");
}
```

#### 步骤 4: 会话过期处理

`SecurityConfig.java` - 配置会话过期后的行为:
```java
.sessionManagement(session -> session
    .sessionFixation().migrateSession()
    .maximumSessions(-1)
    .sessionRegistry(sessionRegistry())
    .expiredUrl("/")  // 会话过期后跳转到首页
);
```

### 3. 调试信息显示

**HomeController** - 在页面上显示详细的认证状态:
- 当前时间戳
- 会话 ID 和创建时间
- 用户认证状态
- Token 签发和过期时间

**控制台日志** - 输出详细的调试信息:
- 登录成功时的会话注册
- Backchannel logout 请求接收
- 会话失效处理过程

## 使用指南

### 测试单点登录

1. **访问 App A**: http://appa.tbk.com/
2. 点击"通过 Keycloak 登录"
3. 输入 Keycloak 凭证并登录
4. 登录成功后，查看用户信息和调试信息
5. **访问 App B**: http://appb.tbk.com/
6. 应该直接进入 App B，无需重新登录 ✅

### 测试单点登出

1. 确保已在两个应用中登录
2. 在 **App A** 点击"退出登录"
3. 查看 **App B** 的控制台，应该看到 Backchannel logout 日志
4. 刷新 **App B** 页面
5. 应该自动跳转到首页，显示未登录状态 ✅

### 查看调试信息

登录后，在 `/home` 页面底部可以看到：

**认证调试信息面板**:
- ✅ 认证状态
- 当前时间
- 会话 ID
- 会话创建时间
- 会话最后访问时间
- 会话是否新建
- 用户名和用户 ID
- Token 签发时间
- Token 过期时间

**控制台日志**:
```
=== /home 访问调试信息 [Mon Feb 02 01:00:00 CST 2026] ===
时间戳: 1738425600000
会话ID: ABC123...
会话创建时间: Mon Feb 02 01:00:00 CST 2026
用户认证状态: ✅ 已认证
用户名: testuser
用户ID: 12345-67890-abcde
✅ 会话已注册到 SessionRegistry
========================
```

### Backchannel Logout 日志

当收到登出通知时，控制台会显示：
```
========================================
【App B】收到 Backchannel logout 请求
时间: 2026-02-02T01:05:00
✅ 成功解析 logout token
   用户ID (sub): 12345-67890-abcde
   会话ID (sid): xyz789...

--- SessionRegistry 状态 ---
用户 12345-67890-abcde 的会话数量: 1
  会话ID: ABC123...
  是否过期: false

--- 开始使会话失效 ---
使会话失效: ABC123...

总共使 1 个会话失效
Backchannel logout 处理完成
========================================
```

## 关键配置说明

### application.yml

```yaml
server:
  port: 10000  # App A: 10000, App B: 20000
  servlet:
    session:
      cookie:
        same-site: lax
        secure: false

spring:
  application:
    name: app-a  # 或 app-b
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-a  # 或 app-b
            client-secret: <从 Keycloak 复制>
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            post-logout-redirect-uri: "{baseUrl}/"
            scope: openid,profile,email
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/fairy.vip
            user-name-attribute: preferred_username
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/fairy.vip
          jwk-set-uri: http://localhost:8080/realms/fairy.vip/protocol/openid-connect/certs
  session:
    timeout: 30m

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

### SecurityConfig 关键配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            // 1. 配置访问权限
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/error", "/logout/connect/back-channel/keycloak")
                    .permitAll()
                .requestMatchers("/home", "/home/**").authenticated()
                .anyRequest().authenticated()
            )
            // 2. 配置 OAuth2 登录
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler())  // 注册会话
            )
            // 3. 配置会话管理
            .sessionManagement(session -> session
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry())
                .expiredUrl("/")  // 会话过期跳转
            );
    }
    
    // 4. 配置 JwtDecoder 支持 logout+jwt
    @Bean
    public JwtDecoder jwtDecoder() {
        return new LogoutTokenJwtDecoder(jwkSetUri);
    }
}
```

## 故障排查

### 问题 1: 单点登录不工作

**症状**: 在 App A 登录后，访问 App B 仍需要登录

**解决方案**:
1. 确认两个应用使用相同的 `issuer-uri`
2. 确认两个应用使用相同的 Keycloak Realm
3. 清除浏览器 Cookie 并重试
4. 检查 Keycloak 中的 redirect URIs 配置

### 问题 2: 单点登出不工作

**症状**: 在 App A 退出后，App B 仍然保持登录状态

**检查清单**:
- [ ] 两个应用使用不同的 `client-id` (app-a 和 app-b)
- [ ] Keycloak 中为每个客户端配置了 Backchannel logout URL
- [ ] Backchannel logout URL 可以从 Keycloak 服务器访问
- [ ] 应用控制台是否收到 Backchannel logout 请求
- [ ] SessionRegistry 中是否有注册的会话

**调试步骤**:
1. 查看 App B 控制台，确认是否收到 Backchannel logout 请求
2. 如果没有收到，检查 Keycloak 的 Backchannel logout URL 配置
3. 如果收到但会话未失效，检查 SessionRegistry 中的会话注册

### 问题 3: Logout Token 解码失败

**错误信息**: `JOSE header typ (type) logout+jwt not allowed`

**解决方案**:
- 确认使用了自定义的 `LogoutTokenJwtDecoder`
- 确认 JwtDecoder 配置为接受 `logout+jwt` 类型

### 问题 4: 会话过期显示错误页面

**症状**: 显示 "This session has been expired" 错误

**解决方案**:
- 在 SecurityConfig 中添加 `.expiredUrl("/")`
- 会话过期后会自动跳转到首页

### 问题 5: 无法访问域名

**解决方案**:
1. 检查 hosts 文件是否正确配置
2. 使用 `ping appa.tbk.com` 验证域名解析
3. 确认应用正在运行

## 架构说明

### 单点登录流程

```
用户访问 App A
    ↓
App A 检测到未认证
    ↓
重定向到 Keycloak 登录页面
    ↓
用户输入凭证
    ↓
Keycloak 验证成功，创建全局会话
    ↓
返回授权码到 App A
    ↓
App A 用授权码换取 Token
    ↓
OAuth2LoginSuccessHandler 注册会话到 SessionRegistry
    ↓
用户访问 App B
    ↓
App B 重定向到 Keycloak
    ↓
Keycloak 检测到已有会话，直接授权
    ↓
App B 获取 Token，用户自动登录 ✅
```

### 单点登出流程

```
用户在 App A 点击退出
    ↓
App A 调用 Keycloak logout endpoint
    ↓
Keycloak 清除全局会话
    ↓
Keycloak 查找该用户的所有活动客户端
    ↓
向 App A 发送 Backchannel logout 请求
    ↓
App A 的 BackchannelLogoutController 处理请求
    ↓
解码 logout token，获取用户 ID
    ↓
从 SessionRegistry 查找该用户的会话
    ↓
使会话失效
    ↓
向 App B 发送 Backchannel logout 请求
    ↓
App B 的 BackchannelLogoutController 处理请求
    ↓
App B 使会话失效
    ↓
用户访问任何应用都需要重新登录 ✅
```

### 为什么需要独立的客户端？

1. **独立的 Backchannel logout URL**: 每个客户端可以配置自己的回调 URL
2. **会话追踪**: Keycloak 可以追踪每个客户端的会话状态
3. **精确通知**: 登出时 Keycloak 知道要通知哪些应用
4. **更好的隔离**: 每个应用有独立的配置和权限管理

如果使用同一个 client-id，Keycloak 的 Backchannel logout URL 只能配置一个，无法同时通知多个应用。

## 项目文件说明

### 核心 Java 文件

| 文件 | 说明 |
|------|------|
| `SecurityConfig.java` | Spring Security 配置，定义认证规则、会话管理 |
| `OAuth2LoginSuccessHandler.java` | 登录成功处理器，注册会话到 SessionRegistry |
| `LogoutTokenJwtDecoder.java` | 自定义 JWT 解码器，支持 logout+jwt 类型 |
| `BackchannelLogoutController.java` | 处理 Keycloak 的 Backchannel logout 请求 |
| `HomeController.java` | 主页控制器，显示用户信息和调试信息 |

### 配置文件

| 文件 | 说明 |
|------|------|
| `application.yml` | Spring Boot 应用配置，包含 OAuth2 和 Keycloak 配置 |
| `pom.xml` | Maven 项目配置，定义依赖和构建配置 |

### 批处理脚本

| 文件 | 说明 |
|------|------|
| `scripts/start-app-a.bat` | 启动 App A |
| `scripts/start-app-b.bat` | 启动 App B |
| `scripts/start-all.bat` | 同时启动两个应用 |
| `scripts/stop-app-a.bat` | 停止 App A |
| `scripts/stop-app-b.bat` | 停止 App B |
| `scripts/stop-all.bat` | 停止所有应用 |
| `scripts/restart-all.bat` | 重启所有应用 |

## 安全建议

### 开发环境

当前配置适用于开发环境，使用 HTTP 和简单的配置。

### 生产环境

在生产环境中，建议：

1. **使用 HTTPS**:
   ```yaml
   server:
     ssl:
       enabled: true
       key-store: classpath:keystore.p12
       key-store-password: <password>
   ```

2. **启用 Cookie 安全**:
   ```yaml
   server:
     servlet:
       session:
         cookie:
           secure: true
           http-only: true
           same-site: strict
   ```

3. **使用强密钥**: 定期更换 client-secret

4. **配置会话超时**:
   ```yaml
   spring:
     session:
       timeout: 15m  # 根据需求调整
   ```

5. **启用 CSRF 保护**: 已默认启用

6. **配置 CORS**: 限制允许的来源

7. **日志脱敏**: 避免在日志中输出敏感信息

## 扩展功能

### 添加更多应用

1. 复制 `app-a` 或 `app-b` 目录
2. 修改 `application.yml` 中的端口和 client-id
3. 在 Keycloak 中创建新的客户端
4. 配置 Backchannel logout URL
5. 更新 hosts 文件添加新域名

### 集成数据库

添加 Spring Data JPA 依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### 添加 API 接口

创建 REST Controller：
```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/user")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OidcUser user) {
        // 返回用户信息
    }
}
```

### 集成前端框架

可以将 Thymeleaf 替换为：
- React
- Vue.js
- Angular

使用前后端分离架构，后端提供 REST API。

## 常见问题 FAQ

### Q: 可以使用同一个 client-id 吗？

A: 不推荐。使用同一个 client-id 时，Keycloak 的 Backchannel logout URL 只能配置一个，无法实现真正的单点登出。

### Q: Backchannel logout 和 Frontchannel logout 有什么区别？

A:
- **Backchannel logout**: Keycloak 服务器直接向应用服务器发送 HTTP POST 请求，更可靠
- **Frontchannel logout**: 通过浏览器重定向通知应用，依赖浏览器和网络状态

推荐使用 Backchannel logout。

### Q: 如果应用在内网，Keycloak 无法访问怎么办？

A: 可以考虑：
1. 配置反向代理，让 Keycloak 可以访问应用
2. 使用较短的会话超时时间
3. 使用 Frontchannel logout（不太可靠）

### Q: 如何调试 Backchannel logout？

A:
1. 查看应用控制台日志
2. 检查 Keycloak Admin Console 中的 Sessions
3. 使用 curl 手动测试 Backchannel logout URL:
   ```bash
   curl -X POST http://appa.tbk.com/logout/connect/back-channel/keycloak
   ```

### Q: 会话过期时间如何配置？

A: 在 `application.yml` 中配置：
```yaml
spring:
  session:
    timeout: 30m  # 30 分钟
```

同时在 Keycloak 中也可以配置会话超时时间。

## 技术细节

### SessionRegistry 的作用

`SessionRegistry` 是 Spring Security 提供的会话管理接口，用于：
1. 追踪所有活动会话
2. 根据用户 principal 查找会话
3. 使会话失效

在本项目中，我们使用用户的 Keycloak ID (subject) 作为 principal，这样可以：
- 在 Backchannel logout 时根据用户 ID 找到所有会话
- 支持同一用户在多个设备上登录

### Logout Token 的结构

Keycloak 发送的 logout token 是一个 JWT，包含：
```json
{
  "typ": "logout+jwt",  // 类型
  "alg": "RS256"        // 签名算法
}
{
  "sub": "user-id",     // 用户 ID
  "sid": "session-id",  // 会话 ID
  "iss": "http://localhost:8080/realms/fairy.vip",
  "aud": "app-a",
  "iat": 1738425600,
  "exp": 1738425660
}
```

### 为什么需要自定义 JwtDecoder？

Spring Security 的默认 `NimbusJwtDecoder` 只接受 `typ: JWT` 的 token，而 Keycloak 的 logout token 使用 `typ: logout+jwt`。

我们的 `LogoutTokenJwtDecoder` 通过配置 `JWSTypeVerifier` 来接受两种类型：
```java
processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
    new JOSEObjectType("JWT"),
    new JOSEObjectType("logout+jwt")
));
```

### CSRF 保护

项目中启用了 CSRF 保护，但对 Backchannel logout URL 进行了例外处理：
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
    .ignoringRequestMatchers("/logout/connect/back-channel/keycloak")
)
```

这是因为 Keycloak 发送的 Backchannel logout 请求不包含 CSRF token。

## 性能优化

### 1. 会话管理

当前配置允许无限会话 (`maximumSessions(-1)`)，在生产环境中可以限制：
```java
.sessionManagement(session -> session
    .maximumSessions(1)  // 每个用户只允许一个会话
    .maxSessionsPreventsLogin(false)  // 新登录踢掉旧会话
)
```

### 2. Token 缓存

可以配置 JwtDecoder 缓存 JWK Set：
```java
NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
    .cache(new ConcurrentMapCache("jwks"))
    .build();
```

### 3. 日志级别

生产环境中将日志级别改为 INFO：
```yaml
logging:
  level:
    org.springframework.security: INFO
```

## 监控和运维

### 健康检查

Spring Boot Actuator 提供健康检查端点：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

访问: http://localhost:10000/actuator/health

### 查看活动会话

可以添加管理端点查看 SessionRegistry：
```java
@GetMapping("/admin/sessions")
public List<String> getActiveSessions() {
    return sessionRegistry.getAllPrincipals().stream()
        .map(Object::toString)
        .collect(Collectors.toList());
}
```

### 日志收集

建议使用 ELK Stack 或类似工具收集和分析日志。

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 更新日志

### v1.0.0 (2026-02-02)
- ✅ 实现单点登录（SSO）
- ✅ 实现单点登出（SLO）
- ✅ 支持 Backchannel logout
- ✅ 添加会话管理和注册
- ✅ 添加调试信息显示
- ✅ 支持 logout+jwt token 解码
- ✅ 会话过期自动跳转

## 联系方式

如有问题，请通过以下方式联系:
- 提交 GitHub Issue
- 发送邮件至项目维护者

---

**注意**: 本项目仅用于学习和演示目的，生产环境使用前请进行充分的安全评估和测试。
