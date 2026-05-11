package org.example.twelfth_module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.twelfth_module.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    List<User> selectAll();
}
