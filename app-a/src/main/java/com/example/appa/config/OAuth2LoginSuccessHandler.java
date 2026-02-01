package com.example.appa.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final SessionRegistry sessionRegistry;

    public OAuth2LoginSuccessHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
        setDefaultTargetUrl("/home");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        // 获取用户主体（使用 subject 作为 principal）
        Object principal = authentication.getPrincipal();
        String userId = null;
        
        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            userId = oidcUser.getSubject(); // 使用 Keycloak 的用户 ID
        }
        
        if (userId != null) {
            // 注册会话到 SessionRegistry，使用用户 ID 作为 principal
            sessionRegistry.registerNewSession(request.getSession().getId(), userId);
            System.out.println("✅ 会话已注册到 SessionRegistry");
            System.out.println("   用户ID: " + userId);
            System.out.println("   会话ID: " + request.getSession().getId());
        } else {
            System.out.println("⚠️ 无法获取用户ID，会话未注册");
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
