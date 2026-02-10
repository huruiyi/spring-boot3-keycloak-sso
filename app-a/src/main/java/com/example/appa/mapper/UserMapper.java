package com.example.appa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.appa.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
