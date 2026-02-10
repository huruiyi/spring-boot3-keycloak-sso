package com.example.appa.service;

import com.example.appa.mapper.UserMapper;
import com.example.appa.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAssociationService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 通过Keycloak用户ID查找本地用户
     */
    public User findUserByKeycloakId(String keycloakUserId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("keycloak_user_id", keycloakUserId);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 通过用户名查找本地用户
     */
    public User findUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 检查用户是否已关联
     */
    public boolean isUserAssociated(String keycloakUserId) {
        return findUserByKeycloakId(keycloakUserId) != null;
    }
}