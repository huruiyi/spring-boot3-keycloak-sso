package com.example.appb.controller;

import com.example.appb.model.User;
import com.example.appb.service.KeycloakService;
import com.example.appb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private KeycloakService keycloakService;

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user);
            
            // 添加成功消息
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            redirectAttributes.addFlashAttribute("username", user.getUsername());
            redirectAttributes.addFlashAttribute("password", user.getPassword());
            
            // 重定向到注册成功页面
            return "redirect:/users/registration-success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/register";
        }
    }

    @GetMapping("/registration-success")
    public String registrationSuccess() {
        return "registration-success";
    }
    
    @GetMapping("/test-keycloak")
    public ResponseEntity<String> testKeycloakConnection() {
        try {
            // 测试用户名检查功能
            boolean exists = keycloakService.isUsernameExists("admin");
            if (exists) {
                return ResponseEntity.ok("✅ Keycloak连接成功！可以查询用户。");
            } else {
                return ResponseEntity.ok("✅ Keycloak连接成功！但未找到测试用户。");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Keycloak连接失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
