package org.example.twelfth_module.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Drug {
    private Long id;
    private String drugCode; // 药品编码
    private String drugName; // 药品名称
    private String genericName; // 通用名
    private String specification; // 规格
    private String unit; // 单位
    private String manufacturer; // 生产厂家
    private String category; // 药品分类
    private String contraindication; // 禁忌症
    private Integer status; // 0停用 1在用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
