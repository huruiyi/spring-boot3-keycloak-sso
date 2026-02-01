# Keycloak 单点登录演示系统

基于 Spring Boot 3 和 Keycloak 的单点登录（SSO）演示项目，包含两个独立的应用和 Nginx 反向代理配置。

## 项目结构

```
sso-demo/
├── app-a/                 # 应用 A（端口 10000）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/appa/
│   │   │   │       ├── AppAApplication.java
│   │   │   │       ├── config/
│   │   │   │       │   └── SecurityConfig.java
│   │   │   │       └── controller/
│   │   │   │           └── HomeController.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── templates/
│   │   │           ├── index.html
│   │   │           ├── home.html
│   │   │           └── fragments/
│   │   │               └── head.html
│   └── pom.xml
│
├── app-b/                 # 应用 B（端口 20000）
│   └── (结构与 app-a 相同)
│
├── nginx/                 # Nginx 配置
│   └── nginx.conf
│
├── hosts-setup/           # Hosts 配置说明
│   └── hosts.txt
│
└── pom.xml               # 父 POM 文件
```

## 技术栈

- **Spring Boot**: 3.2.0
- **Java**: 17
- **Spring Security**: OAuth 2.0 / OpenID Connect
- **Thymeleaf**: 模板引擎
- **Keycloak**: 身份认证服务器
- **Nginx**: 反向代理

## 功能特性

- ✅ 基于 OAuth 2.0 和 OpenID Connect 的单点登录
- ✅ 两个独立应用共享同一个 Keycloak Realm
- ✅ 应用间无缝切换，无需重复登录
- ✅ 统一登出功能
- ✅ Nginx 反向代理配置
- ✅ 响应式 UI 设计

## 前置要求

1. **JDK 17+**
2. **Maven 3.6+**
3. **Keycloak 服务器** (已配置: http://localhost:8080/realms/fairy.vip)
4. **Nginx** (可选，用于域名访问)
5. 管理员权限（用于修改 hosts 文件）

## Keycloak 配置

确保在 Keycloak 中配置了以下客户端：

### App A 配置
- **客户端 ID**: `app-a`
- **客户端密钥**: `K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg`
- **授权类型**: `authorization_code`
- **有效重定向 URI**:
  - `http://appa.tbk.com/login/oauth2/code/keycloak`
  - `http://localhost:10000/login/oauth2/code/keycloak`

### App B 配置
- **客户端 ID**: `app-b`
- **客户端密钥**: `K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg`
- **授权类型**: `authorization_code`
- **有效重定向 URI**:
  - `http://appb.tbk.com/login/oauth2/code/keycloak`
  - `http://localhost:20000/login/oauth2/code/keycloak`

## 快速开始

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

### 2. 配置 Nginx（可选）

如果需要通过域名访问：

**Windows:**
```bash
# 编辑 C:\nginx\conf\nginx.conf
# 将 nginx/nginx.conf 中的内容添加到 http 块中
```

**Linux:**
```bash
# 复制配置文件
sudo cp nginx/nginx.conf /etc/nginx/sites-available/sso-demo

# 创建符号链接
sudo ln -s /etc/nginx/sites-available/sso-demo /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 3. 编译和运行

**编译项目:**
```bash
# 在项目根目录执行
mvn clean package
```

**运行应用 A:**
```bash
cd app-a
mvn spring-boot:run

# 或使用编译后的 JAR
java -jar target/app-a-1.0.0.jar
```

**运行应用 B:**
```bash
cd app-b
mvn spring-boot:run

# 或使用编译后的 JAR
java -jar target/app-b-1.0.0.jar
```

### 4. 访问应用

- **App A**: http://appa.tbk.com/ 或 http://localhost:10000/
- **App B**: http://appb.tbk.com/ 或 http://localhost:20000/

## 使用说明

1. **首次登录**:
   - 访问任意应用（如 App A）
   - 点击"通过 Keycloak 登录"
   - 输入 Keycloak 凭证
   - 登录成功后跳转到应用首页

2. **应用间切换**:
   - 在 App A 首页点击"跳转到 App B"
   - 直接进入 App B，无需重新登录
   - 反之亦然

3. **退出登录**:
   - 点击右上角"退出登录"
   - 将退出 Keycloak 会话
   - 所有应用都将重新要求登录

## 工作原理

### 认证流程

1. 用户访问需要认证的页面
2. 应用将用户重定向到 Keycloak 登录页面
3. 用户在 Keycloak 输入凭证
4. Keycloak 验证成功后返回授权码
5. 应用使用授权码换取访问令牌和 ID Token
6. 应用验证 ID Token 并建立用户会话

### 单点登录原理

- 两个应用共享同一个 Keycloak Realm
- 使用相同的 Keycloak issuer-uri
- Keycloak 通过 Cookie 和 Session 管理会话
- 应用间通过重定向 URL 传递认证状态
- 已认证用户访问其他应用时自动跳过登录

## 配置说明

### application.yml 关键配置

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: app-a  # 或 app-b
            client-secret: K2ak7FFE1t0cFnU9h47CiAVhfDvNTBbg
            authorization-grant-type: authorization_code
            redirect-uri: http://appa.tbk.com/login/oauth2/code/keycloak
            scope: openid,profile,email
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/fairy.vip
            user-name-attribute: preferred_username
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/fairy.vip
```

### SecurityConfig 配置说明

- 允许访问首页（/）、错误页面和静态资源
- 其他所有页面都需要认证
- 配置 OAuth2 登录页面和成功后的跳转
- 配置登出后重定向到 Keycloak 登出端点

## 故障排查

### 问题 1: 无法访问域名

**解决方案:**
- 检查 hosts 文件是否正确配置
- 使用 `ping appa.tbk.com` 验证域名解析
- 如果使用 Nginx，检查 Nginx 是否正在运行

### 问题 2: OAuth2 登录失败

**解决方案:**
- 检查 Keycloak 服务器是否可访问
- 验证客户端 ID 和密钥是否正确
- 检查重定向 URI 是否在 Keycloak 中正确配置
- 查看应用日志获取详细错误信息

### 问题 3: 单点登录不工作

**解决方案:**
- 确认两个应用使用相同的 Keycloak Realm
- 检查 issuer-uri 是否一致
- 清除浏览器 Cookie 并重试
- 检查应用日志中的认证流程

### 问题 4: 端口冲突

**解决方案:**
- 确保 10000 和 20000 端口未被占用
- 如需修改端口，在 application.yml 中调整
- 记得同步更新 Nginx 配置

## 安全建议

1. **生产环境**:
   - 使用 HTTPS 而非 HTTP
   - 配置 SSL 证书
   - 使用更强的客户端密钥
   - 启用 CSRF 保护
   - 定期更新依赖包

2. **Keycloak 配置**:
   - 启用强密码策略
   - 配置多因素认证（MFA）
   - 设置合理的会话超时时间
   - 定期审计日志

## 扩展建议

1. **添加更多应用**: 按照相同模式创建新模块
2. **数据库集成**: 添加用户数据和业务逻辑
3. **API 网关**: 使用 Spring Cloud Gateway
4. **监控**: 集成 Prometheus 和 Grafana
5. **日志**: 使用 ELK Stack 集中管理日志

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请通过以下方式联系:
- 提交 GitHub Issue
- 发送邮件至项目维护者
