package com.example.appb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.appb.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
