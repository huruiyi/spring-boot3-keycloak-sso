package com.example.appa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class AutoLoginService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:fairy.vip}")
    private String realm;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    public String performAutoLogin(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // 获取session cookie并设置重定向URL
                return "/home";
            }
        } catch (Exception e) {
            // 如果自动登录失败，返回到正常登录流程
            return null;
        }
        
        return null;
    }
}