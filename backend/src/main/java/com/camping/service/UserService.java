package com.camping.service;

import com.camping.dto.UserInfoDTO;
import com.camping.dto.UserLoginDTO;
import com.camping.dto.UserRegisterDTO;
import com.camping.entity.User;
import java.util.Map;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(UserRegisterDTO dto) throws Exception;

    /**
     * 用户登录
     */
    UserInfoDTO login(UserLoginDTO dto) throws Exception;

    /**
     * 获取用户信息
     */
    User getUserById(Long userId) throws Exception;

    /**
     * 更新用户信息
     */
    void updateUserInfo(User user) throws Exception;
    void updateUserInfo(UserInfoDTO userInfoDTO) throws Exception;

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword) throws Exception;
}
