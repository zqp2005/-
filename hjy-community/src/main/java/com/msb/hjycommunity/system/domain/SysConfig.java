package com.msb.hjycommunity.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 参数配置对象 sys_config
 */
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 参数主键 */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long configId;

    /** 参数名称 */
    private String configName;

    /** 参数键名 */
    private String configKey;

    /** 参数键值 */
    private String configValue;

    /** 系统内置 */
    private String configType;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    @Override
    public String toString() {
        return "SysConfig{" +
                "configId=" + configId +
                ", configName='" + configName + '\'' +
                ", configKey='" + configKey + '\'' +
                ", configType='" + configType + '\'' +
                '}';
    }
}
