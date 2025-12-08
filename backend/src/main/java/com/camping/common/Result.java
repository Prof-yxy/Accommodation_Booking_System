package com.camping.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 1:成功, 0:失败
    private String msg;
    private T data;

    // 省略静态构建方法 success(data), error(msg) 等
    public static <T> Result<T> success(T data) {
        return new Result<>(1, "success", data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }
}