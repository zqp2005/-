package com.msb.hjycommunity.common.core.domain;

import java.io.Serializable;

public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;

    private int code;
    private String msg;
    private T data;

    private static final String DEFAULT_SUCCESS_MSG = "操作成功";
    private static final String DEFAULT_ERROR_MSG = "操作失败";

    private static final R<?> SUCCESS_INSTANCE = new R<>(SUCCESS, DEFAULT_SUCCESS_MSG, null);

    private R() {
    }

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, DEFAULT_SUCCESS_MSG, null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, DEFAULT_SUCCESS_MSG, data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS, msg, data);
    }

    public static <T> R<T> ok(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static <T> R<T> error() {
        return new R<>(ERROR, DEFAULT_ERROR_MSG, null);
    }

    public static <T> R<T> error(String msg) {
        return new R<>(ERROR, msg, null);
    }

    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> error(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public boolean isSuccess() {
        return SUCCESS == this.code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
