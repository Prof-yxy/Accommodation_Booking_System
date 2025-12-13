package com.camping.service.impl;

import com.camping.common.StringConstants;
import com.camping.dto.UserInfoDTO;
import com.camping.dto.UserLoginDTO;
import com.camping.dto.UserRegisterDTO;
import com.camping.entity.User;
import com.camping.entity.OperationLog;
import com.camping.mapper.OperationLogMapper;
import com.camping.mapper.UserMapper;
import com.camping.service.UserService;
import com.camping.util.JwtUtil;
import com.camping.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    /**
     * 用户注册
     */
    @Autowired
    OperationLogMapper operationLogMapper;
    @Autowired
    UserMapper userMapper;

    @Override
    public void register(UserRegisterDTO dto) throws Exception {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new Exception(StringConstants.USER_USERNAME_NULL);
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new Exception(StringConstants.USER_PASSWORD_NULL);
        }
        if (dto.getPhone() == null ||dto.getPhone().isEmpty()) {
            throw new Exception(StringConstants.USER_PHONE_NULL);
        }

        // 查询是否用户已存在
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new Exception(StringConstants.USER_USERNAME_DUPLICATE);
        }
        // 密码加密 (使用 BCryptPasswordEncoder)
        String encryptedPassword = PasswordUtil.encryptPassword(dto.getPassword());
        // 创建新用户记录到数据库
        LocalDateTime currentDateTime = LocalDateTime.now();
        OperationLog operationLog = new OperationLog(
                StringConstants.OPERATION_USER_REGISTER,
                null,
                StringConstants.OPERATOR_SYSTEM,
                StringConstants.OPERATION_USER_REGISTER,
                StringConstants.OPERATION_USER_REGISTER,
                currentDateTime
        );

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encryptedPassword);
        user.setPhone(dto.getPhone());
        user.setRole(StringConstants.USER_USER); // 普通用户
        user.setCreateTime(currentDateTime);
        user.setUpdateTime(currentDateTime);

        // 插入数据库
        operationLogMapper.insert(operationLog);
        userMapper.insert(user);
    }

    /**
     * 用户登录
     */
    @Override
    public UserInfoDTO login(UserLoginDTO dto) throws Exception {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            throw new Exception(StringConstants.USER_USRN_PSWD_NULL);
        }

        // 查询用户
        User currentUser = userMapper.selectByUsername(dto.getUsername());
        if (currentUser == null) {
            throw new Exception(StringConstants.USER_NOT_EXIST);
        }
        // 验证密码
        if (!PasswordUtil.verifyPassword(dto.getPassword(), currentUser.getPassword())) {
            throw new Exception(StringConstants.USER_PASSWORD_ERR);
        }
        // 生成 JWT Token
        String token = JwtUtil.generateToken(currentUser.getUserId(), currentUser.getUsername(), currentUser.getRole());

        UserInfoDTO userInfoDTO = getUserInfoDTO(currentUser, token);

        return userInfoDTO;
    }

    private static UserInfoDTO getUserInfoDTO(User currentUser, String token) {
        UserInfoDTO userInfoDTO = new UserInfoDTO(
                currentUser.getUserId(),
                currentUser.getUsername(),
                token,
                currentUser.getRole(),
                currentUser.getPhone(),
                currentUser.getCreateTime(),
                currentUser.getUpdateTime()
        );
        userInfoDTO.setUserId(currentUser.getUserId());
        userInfoDTO.setUsername(currentUser.getUsername());
        userInfoDTO.setToken(token);
        userInfoDTO.setRole(currentUser.getRole());
        return userInfoDTO;
    }

    /**
     * 获取用户信息
     */
    @Override
    public User getUserById(Long userId) throws Exception {
        return userMapper.selectById(userId);
    }

    /**
     * 更新用户信息
     */
    @Override
    public void updateUserInfo(User user) throws Exception {
        if (user.getUserId() == null) {
            throw new Exception(StringConstants.USER_UID_NULL);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateUserInfo(UserInfoDTO userInfoDTO) throws Exception {
        User updateUser = userMapper.selectById(userInfoDTO.getUserId());
        String userToken = userInfoDTO.getToken();

        if (JwtUtil.isTokenExpired(userToken)) {
            throw new Exception(StringConstants.USER_TOKEN_EXPIRED);
        }

        updateUser.setUsername(userInfoDTO.getUsername());
        updateUser.setPhone(userInfoDTO.getPhone());
        // 写入数据库
        updateUserInfo(updateUser);
    }

    /**
     * 修改密码
     */
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) throws Exception {
        if (userId == null) {
            throw new Exception(StringConstants.USER_UID_NULL);
        }
        if (oldPassword == null || newPassword == null) {
            throw new Exception(StringConstants.USER_PASSWORD_NULL);
        }
        if (oldPassword.equals(newPassword)) {
            throw new Exception(StringConstants.USER_PASSWORD_DUP);
        }

        // 查询用户
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new Exception(StringConstants.USER_NOT_EXIST);
        }
        // 验证旧密码
        if (!PasswordUtil.verifyPassword(oldPassword, currentUser.getPassword())) {
            throw new Exception(StringConstants.USER_PASSWORD_ERR);
        }
        // 更新新密码
        currentUser.setPassword(PasswordUtil.encryptPassword(newPassword));
        updateUserInfo(currentUser);
    }
}
