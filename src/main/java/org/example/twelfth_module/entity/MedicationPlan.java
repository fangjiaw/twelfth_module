package org.example.twelfth_module.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MedicationPlan {
    private Long id;
    private Long userId;
    private Long drugId;
    private BigDecimal dosage; // 剂量
    private String unit; // 剂量单位
    private String frequency; // 频次
    private String timeSlot; // 时段：morning/afternoon/evening
    private LocalTime scheduledTime; // 计划时间点
    private LocalDate startDate; // 计划开始日期
    private LocalDate endDate; // 计划结束日期
    private Integer courseDays; // 疗程天数
    private String remark;
    private Integer status; // 0停止 1进行中
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联查询用（不映射数据库字段）
    private String drugName;
    private String drugSpecification;
}
