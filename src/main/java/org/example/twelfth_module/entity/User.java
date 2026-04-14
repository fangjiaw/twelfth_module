package org.example.twelfth_module.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private Integer age;
    private Integer gender; // 0女 1男
    private String phone;
    private String allergyHistory; // 过敏史
    private String medicalHistory; // 病史
    private Integer status; // 0禁用 1正常
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
