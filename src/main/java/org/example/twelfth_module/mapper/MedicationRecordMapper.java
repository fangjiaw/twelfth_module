package org.example.twelfth_module.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.twelfth_module.dto.DrugUsageStat;
import org.example.twelfth_module.entity.MedicationRecord;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MedicationRecordMapper {

    /**
     * 查询用户在指定日期范围内的服药记录
     */
    List<MedicationRecord> selectByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询用户在指定日期的服药记录
     */
    List<MedicationRecord> selectByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate);

    /**
     * 统计已服数量
     */
    Integer countCompleted(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计漏服数量
     */
    Integer countMissed(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计应服总量
     */
    Integer countTotal(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按状态统计
     */
    Integer countByStatus(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Integer status);

    /**
     * 获取按时服用的数量（达标）
     */
    Integer countOnTime(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期分组统计
     */
    List<MedicationRecord> selectDailyStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按药品统计使用次数
     */
    List<DrugUsageStat> selectDrugUsageStats(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按时段分组统计（早/中/晚）
     */
    List<MedicationRecord> selectTimeSlotStats(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate);

    /**
     * 获取漏服记录列表
     */
    List<MedicationRecord> selectMissedRecords(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取连续打卡天数（从今天往前推算）
     */
    List<MedicationRecord> selectConsecutiveDays(
            @Param("userId") Long userId,
            @Param("limitDays") Integer limitDays);
}
