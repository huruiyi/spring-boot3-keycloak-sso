package com.example.appa.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.time.Instant;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OidcUser user, Model model, HttpServletRequest request) {
        long timestamp = System.currentTimeMillis();
        String sessionId = request.getSession().getId();
        
        System.out.println("\n=== /home 访问调试信息 [" + new Date(timestamp) + "] ===");
        System.out.println("时间戳: " + timestamp);
        System.out.println("会话ID: " + sessionId);
        System.out.println("会话创建时间: " + new Date(request.getSession().getCreationTime()));
        System.out.println("会话最后访问: " + new Date(request.getSession().getLastAccessedTime()));
        System.out.println("会话是否新建: " + request.getSession().isNew());
        System.out.println("请求来源: " + request.getRemoteAddr());
        System.out.println("请求URL: " + request.getRequestURL());
        System.out.println("用户认证状态: " + (user != null ? "✅ 已认证" : "❌ 未认证"));
        
        // 调试信息添加到模型
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("timestamp", new Date(timestamp));
        debugInfo.put("sessionId", sessionId);
        debugInfo.put("sessionCreationTime", new Date(request.getSession().getCreationTime()));
        debugInfo.put("sessionLastAccessTime", new Date(request.getSession().getLastAccessedTime()));
        debugInfo.put("sessionIsNew", request.getSession().isNew());
        debugInfo.put("authenticated", user != null);
        
        // 强制检查用户认证状态
        if (user == null) {
            System.out.println("❌ 错误: 用户未认证但访问了受保护页面");
            System.out.println("❌ 这不应该发生 - SecurityConfig 应该拦截此请求");
            System.out.println("========================\n");
            debugInfo.put("error", "用户未认证但访问了受保护页面");
            model.addAttribute("debugInfo", debugInfo);
            return "redirect:/oauth2/authorization/keycloak";
        }
        
        System.out.println("✅ 用户已认证");
        System.out.println("用户名: " + user.getPreferredUsername());
        System.out.println("用户ID: " + user.getSubject());
        System.out.println("用户邮箱: " + user.getEmail());
        
        Instant issuedAt = user.getIssuedAt();
        Instant expiresAt = user.getExpiresAt();
        
        if (issuedAt != null) {
            System.out.println("ID Token 签发时间: " + Date.from(issuedAt));
            debugInfo.put("tokenIssuedAt", Date.from(issuedAt));
        }
        if (expiresAt != null) {
            System.out.println("ID Token 过期时间: " + Date.from(expiresAt));
            debugInfo.put("tokenExpiresAt", Date.from(expiresAt));
        }
        
        debugInfo.put("username", user.getPreferredUsername());
        debugInfo.put("userId", user.getSubject());
        
        model.addAttribute("username", user.getPreferredUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("name", user.getFullName());
        model.addAttribute("app", "App A");
        model.addAttribute("debugInfo", debugInfo);
        
        System.out.println("========================\n");
        return "home";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        // 这个方法只用于显示登出确认页面，不执行实际登出
        return "logout";
    }

    @GetMapping("/logout-success")
    public String logoutSuccess() {
        System.out.println("=== 显示退出成功页面 ===");
        System.out.println("用户已成功退出，显示反馈页面");
        return "logout-success";
    }

    @GetMapping("/api/session/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sessionStatus(
            @AuthenticationPrincipal OidcUser user,
            HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();
        
        status.put("authenticated", user != null);
        status.put("sessionId", request.getSession().getId());
        status.put("sessionCreationTime", new Date(request.getSession().getCreationTime()));
        status.put("sessionLastAccessTime", new Date(request.getSession().getLastAccessedTime()));
        
        if (user != null) {
            status.put("username", user.getPreferredUsername());
            status.put("userId", user.getSubject());
        }
        
        return ResponseEntity.ok(status);
    }
}
