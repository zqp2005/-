package com.msb.hjycommunity.common.enums;

/**
 * 业务操作类型（取值对应字典 sys_oper_type）
 */
public enum BusinessType {

    /** 其它 */
    OTHER("0", "其它"),

    /** 插入 */
    INSERT("1", "新增"),

    /** 修改 */
    UPDATE("2", "修改"),

    /** 删除 */
    DELETE("3", "删除");

    private final String code;

    private final String info;

    BusinessType(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
