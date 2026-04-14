package org.example.twelfth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 药品使用统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugUsageStat {
    private Long drugId;
    private String drugName;
    private String specification;
    private Integer usageCount; // 使用次数
    private Double usageRate; // 使用占比
}
