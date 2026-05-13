package org.example.medpilot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.medpilot.entity.Drug;

import java.util.List;

@Mapper
public interface DrugMapper {

    List<Drug> selectAll();
}
