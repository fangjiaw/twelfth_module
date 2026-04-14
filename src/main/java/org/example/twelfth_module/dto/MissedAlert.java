package org.example.twelfth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 漏服提醒 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissedAlert {
    private Long recordId;
    private Long drugId;
    private String drugName;
    private LocalDateTime planTime; // 计划时间
    private Integer status; // 状态
    private String statusText; // 状态文本
    private String suggestAction; // 建议操作
    private String missedReason; // 漏服原因
}
