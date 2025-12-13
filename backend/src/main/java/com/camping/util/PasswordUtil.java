package com.camping.util;

import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码和加密工具类
 */
public class PasswordUtil {

    /**
     * 加密密码
     */
    public static String encryptPassword(String password) {
        try {
            return BCrypt.hashpw(password, BCrypt.gensalt());
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String encryptedPassword) {
        if (BCrypt.checkpw(password, encryptedPassword)) {
            System.out.println("登录成功");
            return true;
        }
        else {
            System.out.println("密码错误");
            return false;
        }
    }
}
