package org.example.medpilot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.medpilot.entity.User;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);
}
