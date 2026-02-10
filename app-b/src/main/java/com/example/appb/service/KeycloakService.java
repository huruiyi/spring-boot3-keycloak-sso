package com.example.appb.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${keycloak.realm:fairy.vip}")
    private String realm;

    @Value("${keycloak.admin-client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username(adminUsername)
                .password(adminPassword)
                .clientId(adminClientId)
                .build();
    }

    public String createKeycloakUser(String username, String email, String password) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 创建用户
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(false);

        Response response = usersResource.create(user);
        
        if (response.getStatus() == 409) {
            // 409冲突：用户名或邮箱已存在
            throw new RuntimeException("用户名或邮箱已存在，请使用其他用户名和邮箱");
        }
        
        if (response.getStatus() != 201) {
            throw new RuntimeException("创建用户失败，HTTP状态码: " + response.getStatus());
        }

        // 获取创建的用户ID
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // 设置密码
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        
        usersResource.get(userId).resetPassword(passwordCred);

        return userId;
    }

    public boolean isUsernameExists(String username) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            return !usersResource.search(username, true).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}