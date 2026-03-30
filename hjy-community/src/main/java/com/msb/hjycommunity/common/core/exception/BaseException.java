package com.msb.hjycommunity.common.core.exception;

public class BaseException extends RuntimeException{
private String code;
private String defaultMessage;
    public BaseException(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public BaseException() {
    }

    public BaseException(String code, String defaultMessage) {
        super(defaultMessage);
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

}
