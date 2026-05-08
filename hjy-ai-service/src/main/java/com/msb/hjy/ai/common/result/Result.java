package com.msb.hjy.ai.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 所有 API 接口统一返回此格式，包含 code（状态码）、message（消息）、
 * data（数据体）和 timestamp（时间戳），保证前后端交互格式一致。
 *
 * @param <T> 数据体类型
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 状态码：200 成功，其他为失败 */
    private int code;
    /** 提示消息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 响应时间戳 */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 无数据成功响应 */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 带数据成功响应 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 带自定义消息和数据的成功响应 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 默认错误响应（500） */
    public static <T> Result<T> error() {
        return new Result<>(500, "服务器内部错误", null);
    }

    /** 带自定义错误消息的响应 */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /** 带自定义状态码和错误消息的响应 */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 完全自定义响应 */
    public static <T> Result<T> of(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /** 判断是否成功 */
    public boolean isSuccess() {
        return this.code == 200;
    }
}
