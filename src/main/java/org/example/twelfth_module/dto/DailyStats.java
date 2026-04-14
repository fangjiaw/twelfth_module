package org.example.twelfth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * 每日统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStats {
    private LocalDate date;
    private Integer planCount; // 应服数量
    private Integer doneCount; // 已服数量
    private Integer missedCount; // 漏服数量
    private Integer supplementCount; // 补服数量
    private Double completionRate; // 完成率
}
