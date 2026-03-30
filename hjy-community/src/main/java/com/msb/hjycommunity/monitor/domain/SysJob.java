package com.msb.hjycommunity.monitor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 定时任务对象
 */
public class SysJob extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long jobId;

    /** 任务名称 */
    private String jobName;

    /** 任务组名 */
    private String jobGroup;

    /** 调用目标字符串 */
    private String invokeTarget;

    /** cron表达式 */
    private String cronExpression;

    /** cron执行策略 */
    private String misfirePolicy;

    /** 是否并发 */
    private String concurrent;

    /** 任务状态 */
    private String status;

    /** 下次执行时间 */
    private String nextValidTime;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getInvokeTarget() {
        return invokeTarget;
    }

    public void setInvokeTarget(String invokeTarget) {
        this.invokeTarget = invokeTarget;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    public void setMisfirePolicy(String misfirePolicy) {
        this.misfirePolicy = misfirePolicy;
    }

    public String getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(String concurrent) {
        this.concurrent = concurrent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextValidTime() {
        return nextValidTime;
    }

    public void setNextValidTime(String nextValidTime) {
        this.nextValidTime = nextValidTime;
    }
}
