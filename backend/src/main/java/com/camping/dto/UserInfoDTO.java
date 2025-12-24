package com.camping.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * User info DTO
 */
public class UserInfoDTO {
    private Long userId;
    private String username;
    private String token;
    private String role;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UserInfoDTO() {
    }

    public UserInfoDTO(Long userId,
            String username,
            String token,
            String role,
            String phone,
            LocalDateTime createTime,
            LocalDateTime updateTime) {
        this.userId = userId;
        this.username = username;
        this.token = token;
        this.role = role;
        this.phone = phone;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public UserInfoDTO(Map<String, Object> userInfoMap) {
        this.userId = (Long) userInfoMap.get("userId");
        this.username = (String) userInfoMap.get("username");
        this.token = (String) userInfoMap.get("token");
        this.role = (String) userInfoMap.get("role");
        this.phone = (String) userInfoMap.get("phone");
        this.createTime = (LocalDateTime) userInfoMap.get("createTime");
        this.updateTime = (LocalDateTime) userInfoMap.get("updateTime");
    }

    public Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("userId", userId);
        ret.put("username", username);
        ret.put("token", token);
        ret.put("role", role);
        ret.put("phone", phone);
        ret.put("createTime", createTime);
        ret.put("updateTime", updateTime);
        return ret;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
