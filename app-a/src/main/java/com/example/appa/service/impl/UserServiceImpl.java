package com.example.appa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.appa.mapper.UserMapper;
import com.example.appa.model.User;
import com.example.appa.service.KeycloakService;
import com.example.appa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private KeycloakService keycloakService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(User user) {
        // 1. 检查本地数据库中用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名或邮箱已存在，请使用其他用户名和邮箱");
        }
        
        // 2. 加密密码用于本地数据库存储
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        
        // 3. 在Keycloak中创建用户（使用原始密码）
        // 如果用户名或邮箱已存在，createKeycloakUser会抛出异常
        String keycloakUserId = keycloakService.createKeycloakUser(
            user.getUsername(), 
            user.getEmail(), 
            user.getPassword()  // Keycloak使用原始密码，它会自己加密
        );
        
        // 4. 保存Keycloak用户ID到本地用户实体
        user.setKeycloakUserId(keycloakUserId);
        
        // 5. 设置加密后的密码用于本地数据库
        user.setPassword(encodedPassword);
        
        // 6. 在本地数据库保存用户信息
        userMapper.insert(user);
    }
}
