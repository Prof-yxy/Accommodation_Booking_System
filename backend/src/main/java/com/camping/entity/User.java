package com.camping.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long userId;
    private String username;
    private String password;
    private String phone;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public User() {
    }
}
