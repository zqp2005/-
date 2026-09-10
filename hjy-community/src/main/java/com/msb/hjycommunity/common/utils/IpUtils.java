package com.msb.hjycommunity.common.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * 客户端IP/归属地/浏览器信息工具
 */
public class IpUtils {

    /**
     * 获取客户端真实IP，优先读取反向代理头（多级代理取第一个）
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return isUnknown(ip) ? "unknown" : ip;
    }

    /**
     * 根据IP简单判断登录地点（内网/未知，无离线IP库）
     */
    public static String getLoginLocation(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "unknown";
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)
                || ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return "内网IP";
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return "内网IP";
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return "unknown";
    }

    /**
     * 从 User-Agent 中简单解析浏览器类型
     */
    public static String getBrowser(HttpServletRequest request) {
        String ua = userAgent(request);
        if (ua.isEmpty()) {
            return "unknown";
        }
        if (ua.contains("Edg/") || ua.contains("Edge/")) {
            return "Edge";
        }
        if (ua.contains("Chrome")) {
            return "Chrome";
        }
        if (ua.contains("Firefox")) {
            return "Firefox";
        }
        if (ua.contains("MSIE") || ua.contains("Trident")) {
            return "IE";
        }
        if (ua.contains("PostmanRuntime")) {
            return "Postman";
        }
        if (ua.contains("Safari")) {
            return "Safari";
        }
        return "unknown";
    }

    /**
     * 从 User-Agent 中简单解析操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String ua = userAgent(request);
        if (ua.isEmpty()) {
            return "unknown";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) {
            return "Mac OS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return "unknown";
    }

    private static String userAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ua = request.getHeader("User-Agent");
        return ua == null ? "" : ua;
    }

    private static boolean isUnknown(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip.trim());
    }
}
