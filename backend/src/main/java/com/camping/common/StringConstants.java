package com.camping.common;

public class StringConstants {

    /* User */
    static public final String USER_UID_NULL = "User Id 不能为空";
    static public final String USER_USERNAME_NULL = "用户名不能为空";
    static public final String USER_PASSWORD_NULL = "密码不能为空";
    static public final String USER_PASSWORD_ERR = "密码错误";
    static public final String USER_PASSWORD_DUP = "新密码不能与旧密码相同";
    static public final String USER_USRN_PSWD_NULL = "用户名或密码不能为空";
    static public final String USER_PHONE_NULL = "电话不能为空";
    static public final String USER_USERNAME_DUPLICATE = "用户名已存在";
    static public final String USER_NOT_EXIST = "用户不存在";
    static public final String USER_ADMIN = "admin";
    static public final String USER_USER = "user";
    static public final String USER_TOKEN_EXPIRED = "Token 已过期";

    /* OperationLog */
    // operation
    static public final String OPERATION_INSERT_BOOKING = "INSERT_BOOKING";
    static public final String OPERATION_CANCEL_BOOKING = "CANCEL_BOOKING";
    static public final String OPERATION_USER_REGISTER = "USER_REGISTER";
    static public final String OPERATION_USER_LOGIN = "USER_LOGIN";
    // operator
    static public final String OPERATOR_SYSTEM = "system";
}
