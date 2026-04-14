package org.example.twelfth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时段统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotStats {
    private Double morning;   // 早间完成率
    private Double afternoon; // 下午完成率
    private Double evening;  // 晚间完成率
}
