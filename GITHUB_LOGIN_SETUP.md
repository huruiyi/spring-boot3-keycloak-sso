# GitHub 登录配置指南

本指南将帮助你在Keycloak中配置GitHub作为身份提供商，让App A和App B支持GitHub登录。

---

## 📋 配置步骤总览

1. 在GitHub创建OAuth应用
2. 在Keycloak中配置GitHub Identity Provider
3. 测试GitHub登录功能

---

## 第一步：在GitHub创建OAuth应用

### 1.1 访问GitHub Developer Settings

1. 登录GitHub账户
2. 访问：https://github.com/settings/developers
3. 或者：点击头像 → Settings → Developer settings → OAuth Apps

### 1.2 创建新的OAuth应用

1. 点击 **"New OAuth App"** 按钮
2. 填写应用信息：

```
Application name: Keycloak SSO Demo
Homepage URL: http://localhost:8080
Authorization callback URL: http://localhost:8080/realms/fairy.vip/broker/github/endpoint
```

**重要提示**：
- `fairy.vip` 是你的realm名称
- `github` 是你在Keycloak中配置的Identity Provider的Alias（别名）
- 如果使用域名访问，将localhost:8080替换为你的域名

### 1.3 获取Client ID和Client Secret

1. 点击 **"Register application"**
2. 记录显示的 **Client ID**
3. 点击 **"Generate a new client secret"**
4. 记录生成的 **Client Secret**（只显示一次，请妥善保存）

**示例**：
```
Client ID: Ov23liABCDEFGHIJKLMN
Client Secret: 1234567890abcdef1234567890abcdef12345678
```

---

## 第二步：在Keycloak中配置GitHub Identity Provider

### 2.1 登录Keycloak Admin Console

1. 访问：http://localhost:8080
2. 使用admin账户登录
3. 选择realm：**fairy.vip**

### 2.2 添加GitHub Identity Provider

1. 在左侧菜单中，点击 **"Identity providers"**
2. 点击 **"Add provider"** 下拉菜单
3. 选择 **"GitHub"**

### 2.3 配置GitHub Provider

根据你提供的截图，填写以下信息：

```
Redirect URI: http://localhost:8080/realms/fairy.vip/broker/github/endpoint
（这个是自动生成的，复制它用于GitHub OAuth应用配置）

Alias: github
（必填，用于URL路径，建议使用小写）

Display name: Github Login
（可选，显示在登录页面的按钮文字）

Client ID: 你的GitHub OAuth应用的Client ID
（必填，从GitHub获取）

Client Secret: 你的GitHub OAuth应用的Client Secret
（必填，从GitHub获取）

Display order: 
（可选，控制按钮显示顺序，留空即可）

Base URL:
（可选，留空使用默认值）

API URL:
（可选，留空使用默认值）

JSON Format: Off
（保持默认）
```

### 2.4 高级配置（可选）

点击页面底部的展开按钮，可以配置更多选项：

**同步模式（Sync Mode）**：
- `Import`: 只在首次登录时导入用户信息（推荐）
- `Force`: 每次登录都更新用户信息
- `Legacy`: 兼容旧版本

**信任邮箱（Trust Email）**：
- 启用：信任GitHub提供的邮箱地址
- 建议：启用

**账户链接（Account Linking）**：
- 启用：允许将GitHub账户链接到现有Keycloak账户
- 建议：启用

**首次登录流程（First Login Flow）**：
- 选择：`first broker login`（默认）
- 用于首次通过GitHub登录时的处理流程

### 2.5 保存配置

1. 点击页面底部的 **"Add"** 或 **"Save"** 按钮
2. 确认配置已保存

---

## 第三步：配置Mapper（可选但推荐）

Mapper用于将GitHub的用户信息映射到Keycloak的用户属性。

### 3.1 访问Mapper配置

1. 在Identity Providers列表中，点击 **"github"**
2. 点击 **"Mappers"** 标签页
3. 点击 **"Add mapper"**

### 3.2 添加常用Mapper

#### Mapper 1: 用户名映射
```
Name: username
Sync Mode Override: inherit
Mapper Type: Attribute Importer
Social Profile JSON Field Path: login
User Attribute Name: username
```

#### Mapper 2: 邮箱映射
```
Name: email
Sync Mode Override: inherit
Mapper Type: Attribute Importer
Social Profile JSON Field Path: email
User Attribute Name: email
```

#### Mapper 3: 头像映射
```
Name: avatar
Sync Mode Override: inherit
Mapper Type: Attribute Importer
Social Profile JSON Field Path: avatar_url
User Attribute Name: avatar_url
```

#### Mapper 4: GitHub ID映射
```
Name: github-id
Sync Mode Override: inherit
Mapper Type: Attribute Importer
Social Profile JSON Field Path: id
User Attribute Name: github_id
```

---

## 第四步：测试GitHub登录

### 4.1 访问应用登录页面

**App A**:
```
http://localhost:10000/
或
http://appa.tbk.com/
```

**App B**:
```
http://localhost:20000/
或
http://appb.tbk.com/
```

### 4.2 点击登录

1. 点击 **"通过 Keycloak 登录"** 按钮
2. 在Keycloak登录页面，你应该看到：
   - 用户名/密码登录表单
   - **"Github Login"** 按钮（新增的）

### 4.3 使用GitHub登录

1. 点击 **"Github Login"** 按钮
2. 跳转到GitHub授权页面
3. 点击 **"Authorize"** 授权应用访问你的GitHub信息
4. 首次登录可能需要：
   - 确认用户信息
   - 设置用户名（如果GitHub用户名已被占用）
5. 授权成功后，自动跳转回应用并登录

### 4.4 验证登录状态

登录成功后，你应该能看到：
- 用户主页显示你的GitHub用户名
- 可以在App A和App B之间自由切换（SSO）
- 退出登录功能正常工作

---

## 🔧 故障排查

### 问题1: 点击GitHub登录后显示404

**原因**: Redirect URI配置错误

**解决方案**:
1. 检查Keycloak中的Alias是否为 `github`
2. 检查GitHub OAuth应用的回调URL是否正确：
   ```
   http://localhost:8080/realms/fairy.vip/broker/github/endpoint
   ```
3. 确保realm名称正确（fairy.vip）

### 问题2: GitHub授权后显示错误

**原因**: Client ID或Client Secret错误

**解决方案**:
1. 重新检查GitHub OAuth应用的Client ID
2. 重新生成Client Secret并更新到Keycloak
3. 确保没有多余的空格

### 问题3: 授权成功但无法登录

**原因**: 用户信息映射问题

**解决方案**:
1. 检查Mapper配置
2. 在Keycloak Admin Console中查看用户是否已创建
3. 检查用户的Federated Identity是否正确链接

### 问题4: 邮箱未同步

**原因**: GitHub账户邮箱未公开

**解决方案**:
1. 访问GitHub Settings → Emails
2. 取消勾选 "Keep my email addresses private"
3. 或者在Keycloak中配置为不要求邮箱验证

### 问题5: 重复用户创建

**原因**: 账户链接未启用

**解决方案**:
1. 在GitHub Identity Provider配置中启用 "Account Linking"
2. 配置First Login Flow处理重复账户

---

## 🎨 自定义登录页面

### 修改GitHub按钮显示文字

1. 在Keycloak Admin Console中
2. Identity Providers → github
3. 修改 **Display name** 字段
4. 例如：
   - `使用GitHub登录`
   - `Sign in with GitHub`
   - `GitHub 登录`

### 修改按钮显示顺序

1. 设置 **Display order** 字段
2. 数字越小，显示越靠前
3. 例如：
   - GitHub: 1
   - Google: 2
   - Facebook: 3

---

## 🔐 安全建议

### 生产环境配置

1. **使用HTTPS**
   ```
   Homepage URL: https://your-domain.com
   Callback URL: https://your-domain.com/realms/fairy.vip/broker/github/endpoint
   ```

2. **限制OAuth应用访问范围**
   - 只请求必要的权限
   - 默认GitHub只需要 `user:email` 权限

3. **定期轮换Client Secret**
   - 在GitHub中重新生成Secret
   - 更新到Keycloak配置

4. **启用邮箱验证**
   - 在Keycloak Realm Settings中启用
   - 确保用户邮箱真实有效

5. **配置账户安全策略**
   - 启用两步验证
   - 配置密码策略
   - 设置会话超时

---

## 📊 用户数据流程

```
用户点击"Github Login"
    ↓
跳转到GitHub授权页面
    ↓
用户授权应用访问
    ↓
GitHub返回授权码
    ↓
Keycloak使用授权码获取Access Token
    ↓
Keycloak使用Token获取用户信息
    ↓
Keycloak创建或更新用户
    ↓
Keycloak创建会话
    ↓
用户登录成功，跳转回应用
```

---

## 🔄 多个Identity Provider配置

你可以同时配置多个社交登录：

### GitHub + Google

1. 添加GitHub Provider（按本指南操作）
2. 添加Google Provider：
   - 在Google Cloud Console创建OAuth应用
   - 在Keycloak中添加Google Provider
   - 配置Client ID和Secret

### GitHub + Facebook

1. 添加GitHub Provider（按本指南操作）
2. 添加Facebook Provider：
   - 在Facebook Developers创建应用
   - 在Keycloak中添加Facebook Provider
   - 配置App ID和Secret

### 登录页面显示

配置多个Provider后，登录页面会显示：
```
┌─────────────────────────────┐
│  Username: [______________] │
│  Password: [______________] │
│  [        Login        ]    │
│                             │
│  Or sign in with:           │
│  [  Github Login  ]         │
│  [  Google Login  ]         │
│  [  Facebook Login ]        │
└─────────────────────────────┘
```

---

## 📝 配置检查清单

使用此清单确保配置正确：

### GitHub端
- [ ] 创建了OAuth应用
- [ ] 记录了Client ID
- [ ] 记录了Client Secret
- [ ] 配置了正确的回调URL
- [ ] 应用状态为Active

### Keycloak端
- [ ] 添加了GitHub Identity Provider
- [ ] Alias设置为 `github`
- [ ] 填写了正确的Client ID
- [ ] 填写了正确的Client Secret
- [ ] 保存了配置
- [ ] 配置了Mapper（可选）
- [ ] 启用了Trust Email
- [ ] 启用了Account Linking

### 应用端
- [ ] App A可以访问
- [ ] App B可以访问
- [ ] 登录页面显示GitHub按钮
- [ ] 点击按钮可以跳转到GitHub
- [ ] 授权后可以成功登录
- [ ] SSO功能正常工作

---

## 🎯 快速配置命令

如果你使用Keycloak CLI，可以使用以下命令快速配置：

```bash
# 添加GitHub Identity Provider
kcadm.sh create identity-provider/instances \
  -r fairy.vip \
  -s alias=github \
  -s providerId=github \
  -s enabled=true \
  -s 'config.useJwksUrl="true"' \
  -s config.clientId=YOUR_CLIENT_ID \
  -s config.clientSecret=YOUR_CLIENT_SECRET \
  -s config.defaultScope="user:email"
```

---

## 📚 相关文档

- [Keycloak Identity Brokering](https://www.keycloak.org/docs/latest/server_admin/#_identity_broker)
- [GitHub OAuth Apps](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [Keycloak Mappers](https://www.keycloak.org/docs/latest/server_admin/#_mappers)

---

## ✅ 完成

配置完成后，你的应用将支持：
1. ✅ 传统用户名/密码登录
2. ✅ GitHub社交登录
3. ✅ 单点登录（SSO）
4. ✅ 统一的用户管理

用户可以选择任何一种方式登录，享受无缝的单点登录体验！

---

**配置日期**: 2026-02-10  
**版本**: 1.0.0  
**作者**: Kiro AI Assistant
