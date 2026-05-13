package org.example.twelfth_module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.twelfth_module.entity.Drug;

import java.util.List;

@Mapper
public interface DrugMapper {

    List<Drug> selectAll();
}
