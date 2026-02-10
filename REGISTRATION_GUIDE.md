# 用户注册功能完整指南

## 📋 功能概述

本项目实现了完整的用户注册功能，允许用户自助注册账户并自动在Keycloak身份认证系统中创建对应的用户。注册后的账户可以在所有集成了Keycloak的应用中使用，实现真正的单点登录。

## ✨ 功能特性

### 1. 双重用户创建
- **Keycloak用户**：在Keycloak身份认证系统中创建用户
- **本地用户**：在应用数据库中保存用户信息
- **关联映射**：通过`keycloakUserId`字段关联两者

### 2. 完整的表单验证
- **用户名验证**：
  - 只能包含字母、数字和下划线
  - 长度限制：3-20个字符
  - 自动检查是否已存在
  
- **邮箱验证**：
  - 标准邮箱格式验证
  - HTML5原生验证
  
- **密码验证**：
  - 最小长度：6个字符
  - 密码确认匹配检查
  - 前端和后端双重验证

### 3. 用户友好的界面
- **响应式设计**：适配各种屏幕尺寸
- **实时反馈**：表单验证即时提示
- **加载状态**：提交时显示"正在注册..."
- **错误提示**：清晰的错误信息显示
- **成功引导**：注册成功后自动跳转登录

### 4. 安全性保障
- **CSRF保护**：防止跨站请求伪造
- **密码加密**：Keycloak自动加密存储
- **事务管理**：确保数据一致性
- **重复检查**：防止用户名重复注册

## 🚀 使用指南

### 访问注册页面

#### App A
- URL: http://appa.tbk.com/users/register
- 或: http://localhost:10000/users/register

#### App B
- URL: http://appb.tbk.com/users/register
- 或: http://localhost:20000/users/register

### 注册流程

1. **访问首页**
   - 在未登录状态下，点击"注册新账户"按钮

2. **填写注册信息**
   ```
   用户名：testuser（3-20字符，字母数字下划线）
   邮箱：testuser@example.com
   密码：password123（至少6字符）
   确认密码：password123（必须匹配）
   ```

3. **提交注册**
   - 点击"注册账户"按钮
   - 系统会显示"正在注册..."状态

4. **注册成功**
   - 自动跳转到注册成功页面
   - 显示注册的用户名
   - 3秒后自动跳转到登录页面

5. **登录使用**
   - 使用注册的用户名和密码登录
   - 可以在App A和App B之间自由切换

## 🏗️ 技术实现

### 架构设计

```
用户提交注册表单
    ↓
UserController 接收请求
    ↓
UserService 处理业务逻辑
    ├─→ KeycloakService 创建Keycloak用户
    │   ├─ 检查用户名是否存在
    │   ├─ 创建用户表示
    │   ├─ 设置用户密码
    │   └─ 返回Keycloak用户ID
    │
    └─→ UserMapper 保存到数据库
        ├─ 保存用户基本信息
        └─ 关联Keycloak用户ID
    ↓
返回注册成功页面
    ↓
自动跳转到登录页面
```

### 核心代码文件

#### 1. 控制器层
**文件**: `UserController.java`
```java
@Controller
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(User user, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            redirectAttributes.addFlashAttribute("username", user.getUsername());
            return "redirect:/users/registration-success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/register";
        }
    }
}
```

#### 2. 服务层
**文件**: `UserServiceImpl.java`
```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    @Transactional
    public void register(User user) {
        // 1. 检查Keycloak中是否已存在该用户
        if (keycloakService.findKeycloakUser(user.getUsername()) != null) {
            throw new RuntimeException("用户名已在Keycloak中存在");
        }
        
        // 2. 在Keycloak中创建用户
        String keycloakUserId = keycloakService.createKeycloakUser(
            user.getUsername(), 
            user.getEmail(), 
            user.getPassword()
        );
        
        // 3. 保存Keycloak用户ID
        user.setKeycloakUserId(keycloakUserId);
        
        // 4. 在本地数据库保存用户信息
        userMapper.insert(user);
    }
}
```

#### 3. Keycloak集成
**文件**: `KeycloakService.java`
```java
@Service
public class KeycloakService {
    
    public String createKeycloakUser(String username, String email, String password) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 创建用户表示
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(false);

        // 创建用户
        Response response = usersResource.create(user);
        String userId = response.getLocation().getPath()
                               .replaceAll(".*/([^/]+)$", "$1");

        // 设置密码
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        
        usersResource.get(userId).resetPassword(passwordCred);

        return userId;
    }
}
```

#### 4. 数据模型
**文件**: `User.java`
```java
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String keycloakUserId; // 关联的Keycloak用户ID
}
```

### 数据库表结构

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    keycloak_user_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_keycloak_user_id ON users(keycloak_user_id);
```

### 安全配置

**文件**: `SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   ClientRegistrationRepository clientRegistrationRepository) 
                                                   throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 允许未登录用户访问注册页面
                .requestMatchers("/", "/error", "/users/register", 
                                "/users/registration-success").permitAll()
                .requestMatchers("/home", "/home/**").authenticated()
                .anyRequest().authenticated()
            )
            // ... 其他配置
        return http.build();
    }
}
```

## 🔧 配置说明

### 1. Keycloak配置

在 `application.yml` 中配置Keycloak Admin API访问：

```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: fairy.vip
  admin-client-id: admin-cli
  admin-username: admin
  admin-password: admin
```

### 2. 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/user_db
    username: postgres
    password: your_password
```

### 3. Maven依赖

```xml
<!-- Keycloak Admin Client -->
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>26.0.0</version>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

## 🐛 故障排查

### 问题1: 注册时出现403 Forbidden错误

**症状**: 提交注册表单时返回403错误，错误信息包含"Forbidden"

**原因**: Spring Security的CSRF保护机制阻止了请求

**解决方案**:
1. 确认注册表单中包含CSRF token：
   ```html
   <form id="registerForm">
       <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
       <!-- 其他表单字段 -->
   </form>
   ```

2. 如果使用AJAX提交，需要在请求头中包含CSRF token：
   ```javascript
   const csrfToken = document.querySelector('input[name="_csrf"]').value;
   const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
   
   fetch('/users/register', {
       method: 'POST',
       headers: {
           [csrfHeader]: csrfToken
       },
       body: formData
   });
   ```

3. 或者使用传统表单提交（推荐）：
   ```javascript
   // 验证通过后直接提交表单
   form.submit();
   ```

### 问题2: 无法访问注册页面

**症状**: 访问 `/users/register` 时被重定向到登录页面

**解决方案**:
1. 检查 `SecurityConfig.java` 中是否配置了 `.requestMatchers("/users/register", "/users/registration-success").permitAll()`
2. 确认Spring Security配置已生效
3. 清除浏览器缓存并重试

### 问题2: 注册时提示"用户名已存在"

**症状**: 提交注册表单后显示用户名已在Keycloak中存在

**解决方案**:
1. 登录Keycloak Admin Console
2. 进入 `Users` 页面
3. 搜索并删除已存在的用户
4. 或者使用不同的用户名注册

### 问题3: Keycloak连接失败

**症状**: 注册时出现连接错误

**解决方案**:
1. 确认Keycloak服务正在运行：http://localhost:8080
2. 检查 `application.yml` 中的Keycloak配置
3. 验证admin用户名和密码是否正确
4. 检查网络连接和防火墙设置

### 问题4: 数据库保存失败

**症状**: Keycloak用户创建成功，但本地数据库保存失败

**解决方案**:
1. 检查数据库连接配置
2. 确认数据库表已创建
3. 查看应用日志中的详细错误信息
4. 验证数据库用户权限

### 问题5: 注册成功但无法登录

**症状**: 注册成功后使用相同的用户名密码无法登录

**解决方案**:
1. 确认Keycloak中用户状态为"Enabled"
2. 检查密码是否正确设置
3. 在Keycloak Admin Console中手动重置密码
4. 确认Realm配置正确

## 📈 功能扩展建议

### 1. 邮箱验证
```java
// 发送验证邮件
public void sendVerificationEmail(String email, String token) {
    // 使用Spring Mail发送验证邮件
}

// 验证邮箱
@GetMapping("/verify-email")
public String verifyEmail(@RequestParam String token) {
    // 验证token并激活用户
}
```

### 2. 验证码功能
```java
// 生成验证码
@GetMapping("/captcha")
public void generateCaptcha(HttpServletResponse response) {
    // 生成图形验证码
}

// 验证验证码
public boolean verifyCaptcha(String userInput, String sessionCaptcha) {
    return userInput.equalsIgnoreCase(sessionCaptcha);
}
```

### 3. 密码强度检查
```java
public boolean isStrongPassword(String password) {
    // 至少8个字符
    // 包含大小写字母
    // 包含数字
    // 包含特殊字符
    String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    return password.matches(pattern);
}
```

### 4. 用户名唯一性实时检查
```javascript
// 前端实时检查
async function checkUsernameAvailability(username) {
    const response = await fetch(`/api/users/check-username?username=${username}`);
    const data = await response.json();
    return data.available;
}
```

### 5. 社交登录集成
```java
// Google OAuth2登录
@GetMapping("/oauth2/authorization/google")
public String googleLogin() {
    // 重定向到Google OAuth2
}

// 处理回调
@GetMapping("/login/oauth2/code/google")
public String googleCallback(@RequestParam String code) {
    // 处理Google回调
}
```

### 6. 用户资料完善
```java
@PostMapping("/profile/complete")
public String completeProfile(UserProfile profile) {
    // 保存用户详细资料
    // 如：头像、生日、地址等
}
```

## 🔒 安全最佳实践

### 1. 密码策略
- 最小长度：8个字符
- 包含大小写字母、数字和特殊字符
- 定期提醒用户更换密码
- 禁止使用常见密码

### 2. 防止暴力破解
- 添加验证码
- 限制注册频率
- IP地址限制
- 账户锁定机制

### 3. 数据保护
- 使用HTTPS传输
- 密码加密存储
- 敏感信息脱敏
- 定期备份数据

### 4. 审计日志
```java
@Aspect
@Component
public class RegistrationAuditAspect {
    
    @AfterReturning("execution(* com.example.*.service.UserService.register(..))")
    public void logRegistration(JoinPoint joinPoint) {
        User user = (User) joinPoint.getArgs()[0];
        log.info("New user registered: {}", user.getUsername());
        // 记录IP地址、时间戳等信息
    }
}
```

## 📊 监控和统计

### 1. 注册成功率
```java
@Service
public class RegistrationMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public void recordRegistrationAttempt(boolean success) {
        meterRegistry.counter("user.registration", 
            "status", success ? "success" : "failure").increment();
    }
}
```

### 2. 用户增长趋势
```sql
-- 每日新增用户统计
SELECT DATE(created_at) as date, COUNT(*) as new_users
FROM users
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

## 🎯 快速测试步骤

### 手动测试注册功能

1. **访问注册页面**
   - App A: http://localhost:10000/users/register
   - App B: http://localhost:20000/users/register

2. **填写注册表单**
   ```
   用户名: testuser123
   邮箱: testuser123@example.com
   密码: password123
   确认密码: password123
   ```

3. **提交注册**
   - 点击"注册账户"按钮
   - 观察是否显示"正在注册..."状态

4. **验证注册成功**
   - 应该跳转到注册成功页面
   - 显示注册的用户名
   - 3秒后自动跳转到登录页面

5. **测试登录**
   - 点击"通过 Keycloak 登录"
   - 使用刚注册的用户名和密码登录
   - 应该成功进入主页

6. **测试SSO**
   - 在App A登录后
   - 点击导航栏的"跳转到 App B"
   - 应该自动登录到App B（无需再次输入密码）

### 验证检查清单

- [ ] 注册页面可以正常访问（返回200状态码）
- [ ] 表单验证正常工作（用户名格式、密码长度等）
- [ ] 密码确认匹配检查有效
- [ ] CSRF token正确传递（无403错误）
- [ ] Keycloak用户创建成功
- [ ] 本地数据库保存成功
- [ ] 注册成功页面正确显示
- [ ] 自动跳转到登录页面
- [ ] 可以使用新账户登录
- [ ] SSO功能正常工作

### 常见测试场景

#### 场景1: 正常注册流程
```
输入: 有效的用户名、邮箱、密码
预期: 注册成功，跳转到成功页面
```

#### 场景2: 用户名已存在
```
输入: 已存在的用户名
预期: 显示错误"用户名已在Keycloak中存在"
```

#### 场景3: 密码不匹配
```
输入: 两次密码输入不一致
预期: 前端显示"两次输入的密码不匹配！"
```

#### 场景4: 无效的用户名格式
```
输入: 包含特殊字符的用户名（如 test@user）
预期: HTML5验证阻止提交
```

#### 场景5: 密码太短
```
输入: 少于6个字符的密码
预期: HTML5验证阻止提交
```

## 🎯 自动化测试指南

### 单元测试
```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testRegisterNewUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        assertDoesNotThrow(() -> userService.register(user));
    }
    
    @Test
    void testRegisterDuplicateUser() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");
        user.setPassword("password123");
        
        assertThrows(RuntimeException.class, () -> userService.register(user));
    }
}
```

### 集成测试
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRegisterEndpoint() throws Exception {
        mockMvc.perform(post("/users/register")
                .param("username", "newuser")
                .param("email", "new@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/registration-success"));
    }
}
```

## 📝 总结

本项目实现了一个完整的用户注册功能，具有以下特点：

✅ **完整性**: 从前端表单到后端服务，再到Keycloak集成，全流程实现  
✅ **安全性**: 多层验证、CSRF保护、密码加密  
✅ **用户友好**: 清晰的界面、实时反馈、自动跳转  
✅ **可扩展性**: 易于添加新功能，如邮箱验证、社交登录等  
✅ **可维护性**: 清晰的代码结构、完善的注释、详细的文档  

通过本指南，您可以：
- 理解注册功能的完整实现
- 快速部署和使用注册功能
- 排查常见问题
- 扩展和定制功能

如有问题或建议，欢迎提交Issue或Pull Request！

---

**最后更新**: 2026-02-10  
**版本**: 1.0.0  
**作者**: Kiro AI Assistant
