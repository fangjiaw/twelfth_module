package org.example.twelfth_module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.twelfth_module.entity.MedicationPlan;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MedicationPlanMapper {

    /**
     * 查询用户活跃的用药计划
     */
    List<MedicationPlan> selectActivePlansByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定日期的计划
     */
    List<MedicationPlan> selectPlansByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    /**
     * 查询单个计划
     */
    MedicationPlan selectById(@Param("id") Long id);

    /**
     * 统计用户活跃计划数量
     */
    Integer countActivePlans(@Param("userId") Long userId);
}
