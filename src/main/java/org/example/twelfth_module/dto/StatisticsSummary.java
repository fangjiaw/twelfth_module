package org.example.twelfth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 统计汇总 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsSummary {
    private Double checkInRate;      // 打卡率
    private Double missedRate;       // 漏服率
    private Double complianceRate;   // 达标率（按时服药）
    private Integer consecutiveDays; // 连续打卡天数
    private Integer longestStreak;  // 历史最长连续
    private Integer todayPlanCount;  // 今日应服
    private Integer todayDoneCount;  // 今日已服
    private Integer todayMissedCount; // 今日漏服
    private Map<String, Double> timeSlotStats; // 时段统计
    private String complianceLevel;  // 达标等级：excellent/good/warning
}
