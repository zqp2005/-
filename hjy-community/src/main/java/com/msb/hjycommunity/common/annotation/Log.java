package com.msb.hjycommunity.common.annotation;

import com.msb.hjycommunity.common.enums.BusinessType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义操作日志注解
 * 标注在 Controller 写操作方法上，由 LogAspect 切面拦截并落库 sys_oper_log
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 模块名称 */
    String title() default "";

    /** 业务类型（新增/修改/删除/其它） */
    BusinessType businessType() default BusinessType.OTHER;
}
