package com.camping.controller;

import com.camping.common.Result;
import com.camping.common.StringConstants;
import com.camping.dto.*;
import com.camping.service.UserService;
import com.camping.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 用户模块控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO dto) {
        try {
            userService.register(dto);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO dto) {
        try {
            Map<String, Object> result = userService.login(dto).toMap();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUser(@RequestParam String token) {
        try {
            if (JwtUtil.isTokenExpired(token)) {
                throw new Exception(StringConstants.USER_TOKEN_EXPIRED);
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            var user = userService.getUserById(userId);

            Map<String, Object> info = new java.util.HashMap<>();
            info.put("userId", user.getUserId());
            info.put("username", user.getUsername());
            info.put("phone", user.getPhone());
            info.put("role", user.getRole());
            info.put("createTime", user.getCreateTime());
            info.put("updateTime", user.getUpdateTime());

            return Result.success(info);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestParam String token) {
        // JWT tokens are stateless, logout is handled by removing token on client side
        // For enhanced security, a token blacklist could be implemented here
        return Result.success(null);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UserInfoDTO userInfoDTO) {
        try {
            userService.updateUserInfo(userInfoDTO);

            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, Object> data) {
        /*
         * data: token->String
         * oldPassword->String
         * newPassword->String
         */
        try {
            Long userId = JwtUtil.getUserIdFromToken((String) data.get("token")); // 从 Token 获取
            String oldPassword = (String) data.get("oldPassword");
            String newPassword = (String) data.get("newPassword");

            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }
}