package com.camping.controller;

import com.camping.common.Result;
import com.camping.dto.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    // 注入 Service
    // @Autowired private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO dto) {
        // 实现注册逻辑
        return Result.success(null);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO dto) {
        // 实现登录逻辑，生成 Token
        // 返回结构: {userId, username, token, role}
        return Result.success(null); // 占位
    }
}