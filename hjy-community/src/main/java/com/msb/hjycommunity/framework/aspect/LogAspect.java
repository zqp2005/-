package com.msb.hjycommunity.framework.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.utils.IpUtils;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysOperlog;
import com.msb.hjycommunity.monitor.mapper.SysOperlogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志切面
 * 拦截标注 {@link Log} 注解的 Controller 方法，组装 SysOperlog 同步写入 sys_oper_log 表。
 * 日志记录失败只打印错误，不影响业务主流程。
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 请求参数最大记录长度（字符） */
    private static final int PARAM_MAX_LENGTH = 500;

    /** 返回结果最大记录长度（字符） */
    private static final int RESULT_MAX_LENGTH = 1000;

    /** 错误消息最大记录长度（字符） */
    private static final int ERROR_MSG_MAX_LENGTH = 2000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private SysOperlogMapper operlogMapper;

    /**
     * 切点：所有标注 @Log 注解的方法
     */
    @Pointcut("@annotation(controllerLog)")
    public void logPointCut(Log controllerLog) {
    }

    /**
     * 正常返回后记录操作日志
     */
    @AfterReturning(pointcut = "logPointCut(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 抛出异常后记录操作日志
     */
    @AfterThrowing(pointcut = "logPointCut(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    private void handleLog(final JoinPoint joinPoint, Log controllerLog, Exception e, Object jsonResult) {
        try {
            SysOperlog operlog = new SysOperlog();

            // 注解信息
            operlog.setTitle(controllerLog.title());
            operlog.setBusinessType(controllerLog.businessType().getCode());

            // 方法信息
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            operlog.setMethod(className + "." + methodName + "()");
            operlog.setRequestMethod(getRequestMethod());

            // 操作人信息
            operlog.setOperatorType("1");
            operlog.setOperName(getOperatorName());

            // 请求信息
            operlog.setOperUrl(getRequestUrl());
            operlog.setOperIp(getOperIp());
            operlog.setOperLocation(IpUtils.getLoginLocation(operlog.getOperIp()));

            // 参数与结果
            operlog.setOperParam(getMethodParams(joinPoint));
            operlog.setJsonResult(getJsonResult(jsonResult));

            // 状态：0正常 1异常
            if (e != null) {
                operlog.setStatus(1);
                operlog.setErrorMsg(truncate(e.getMessage(), ERROR_MSG_MAX_LENGTH));
            } else {
                operlog.setStatus(0);
            }
            operlog.setOperTime(new java.util.Date());

            operlogMapper.insertOperlog(operlog);
        } catch (Exception exp) {
            log.error("记录操作日志失败: {}", exp.getMessage(), exp);
        }
    }

    /**
     * 获取当前请求方式（GET/POST/...）
     */
    private String getRequestMethod() {
        HttpServletRequest request = getRequest();
        return request == null ? "" : request.getMethod();
    }

    /**
     * 获取当前请求URI
     */
    private String getRequestUrl() {
        HttpServletRequest request = getRequest();
        return request == null ? "" : request.getRequestURI();
    }

    /**
     * 获取客户端IP
     */
    private String getOperIp() {
        HttpServletRequest request = getRequest();
        return IpUtils.getIpAddr(request);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 获取操作人账号，未认证场景（如权限校验失败）返回 unknown
     */
    private String getOperatorName() {
        try {
            return SecurityUtils.getUserName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 序列化方法入参（过滤Servlet对象/文件/校验结果），截断防超大
     */
    private String getMethodParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile || arg instanceof BindingResult) {
                continue;
            }
            filtered.add(arg);
        }
        if (filtered.isEmpty()) {
            return "";
        }
        try {
            return truncate(OBJECT_MAPPER.writeValueAsString(filtered), PARAM_MAX_LENGTH);
        } catch (Exception ex) {
            return truncate(filtered.toString(), PARAM_MAX_LENGTH);
        }
    }

    /**
     * 序列化返回结果：BaseResponse 只记录 code/msg/success，其它结果整体序列化，截断防超大
     */
    private String getJsonResult(Object jsonResult) {
        if (jsonResult == null) {
            return "";
        }
        try {
            if (jsonResult instanceof BaseResponse) {
                BaseResponse<?> response = (BaseResponse<?>) jsonResult;
                Map<String, Object> simplified = new LinkedHashMap<>();
                simplified.put("code", response.getCode());
                simplified.put("msg", response.getMsg());
                simplified.put("success", response.isSuccess());
                return truncate(OBJECT_MAPPER.writeValueAsString(simplified), RESULT_MAX_LENGTH);
            }
            return truncate(OBJECT_MAPPER.writeValueAsString(jsonResult), RESULT_MAX_LENGTH);
        } catch (Exception ex) {
            return truncate(String.valueOf(jsonResult), RESULT_MAX_LENGTH);
        }
    }

    /**
     * 字符串超长截断
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
