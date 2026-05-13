package org.example.twelfth_module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.twelfth_module.entity.User;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);
}
