package com.example.appa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BackchannelLogoutController {

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    @Autowired(required = false)
    private JwtDecoder jwtDecoder;

    @PostMapping("/logout/connect/back-channel/keycloak")
    public ResponseEntity<String> backchannelLogout(
            @RequestParam(value = "logout_token", required = false) String logoutToken,
            @RequestBody(required = false) String body,
            HttpServletRequest request) {
        
        System.out.println("\n========================================");
        System.out.println("【App A】收到 Backchannel logout 请求");
        System.out.println("时间: " + LocalDateTime.now());
        System.out.println("来源 IP: " + request.getRemoteAddr());
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Logout token (param): " + (logoutToken != null ? "存在" : "不存在"));
        System.out.println("Request body: " + (body != null ? body : "空"));
        
        // 如果 logout_token 在 body 中，尝试解析
        if (logoutToken == null && body != null && body.contains("logout_token=")) {
            String[] parts = body.split("&");
            for (String part : parts) {
                if (part.startsWith("logout_token=")) {
                    logoutToken = part.substring("logout_token=".length());
                    System.out.println("从 body 中提取到 logout_token");
                    break;
                }
            }
        }
        
        // 解析 logout token 获取用户信息
        String userId = null;
        String sessionId = null;
        
        if (logoutToken != null) {
            try {
                if (jwtDecoder != null) {
                    Jwt jwt = jwtDecoder.decode(logoutToken);
                    userId = jwt.getSubject();
                    sessionId = jwt.getClaimAsString("sid");
                    System.out.println("✅ 成功解析 logout token");
                    System.out.println("   用户ID (sub): " + userId);
                    System.out.println("   会话ID (sid): " + sessionId);
                    System.out.println("   所有 claims: " + jwt.getClaims());
                } else {
                    System.out.println("❌ JwtDecoder 未配置");
                }
            } catch (Exception e) {
                System.out.println("❌ 解析 logout token 失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ 未找到 logout_token");
        }
        
        // 检查 SessionRegistry 状态
        if (sessionRegistry != null) {
            System.out.println("\n--- SessionRegistry 状态 ---");
            System.out.println("SessionRegistry 类型: " + sessionRegistry.getClass().getName());
            
            if (userId != null) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(userId, false);
                System.out.println("用户 " + userId + " 的会话数量: " + sessions.size());
                
                for (SessionInformation session : sessions) {
                    System.out.println("  会话ID: " + session.getSessionId());
                    System.out.println("  是否过期: " + session.isExpired());
                    System.out.println("  最后请求时间: " + session.getLastRequest());
                }
            } else {
                System.out.println("无法查询会话：用户ID为空");
            }
        } else {
            System.out.println("❌ SessionRegistry 未配置");
        }
        
        // 使所有相关会话失效
        int invalidatedSessions = 0;
        
        if (sessionRegistry != null && userId != null) {
            try {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(userId, false);
                System.out.println("\n--- 开始使会话失效 ---");
                for (SessionInformation session : sessions) {
                    System.out.println("使会话失效: " + session.getSessionId());
                    session.expireNow();
                    invalidatedSessions++;
                }
            } catch (Exception e) {
                System.out.println("❌ 通过 SessionRegistry 使会话失效失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 尝试使当前请求的会话失效（虽然通常不会有）
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("使当前会话失效: " + session.getId());
            try {
                session.invalidate();
                invalidatedSessions++;
            } catch (Exception e) {
                System.out.println("使当前会话失效失败: " + e.getMessage());
            }
        }
        
        System.out.println("\n总共使 " + invalidatedSessions + " 个会话失效");
        System.out.println("Backchannel logout 处理完成");
        System.out.println("========================================\n");
        
        return ResponseEntity.ok("Logout successful");
    }
}
