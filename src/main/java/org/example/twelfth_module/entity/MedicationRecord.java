package org.example.twelfth_module.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MedicationRecord {
    private Long id;
    private Long userId;
    private Long planId;
    private Long drugId;
    private LocalDateTime scheduledTime; // 计划服药时间
    private LocalDateTime actualTime; // 实际服药时间
    private Integer status; // 0待服 1已服 2漏服 3补服
    private Integer isOnTime; // 0否 1是
    private Integer lateMinutes; // 延迟分钟数
    private String missedReason; // 漏服原因
    private String supplementAction; // 补服建议
    private String remark;
    private LocalDate recordDate; // 记录日期（冗余字段方便统计）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联查询用
    private String drugName;
    private String planTimeSlot;
}
