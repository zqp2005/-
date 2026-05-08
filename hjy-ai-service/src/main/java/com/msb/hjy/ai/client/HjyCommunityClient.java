package com.msb.hjy.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * hjy-community 后端 HTTP 客户端
 * <p>
 * 以管理员身份自动登录主后端系统，缓存 JWT Token，并提供 GET / POST / PUT
 * 等 HTTP 方法的封装，用于 AI 工具调用时获取或写入物业业务数据。
 */
@Slf4j
@Component
public class HjyCommunityClient {

    /** Spring RestTemplate 实例 */
    @Autowired
    private RestTemplate restTemplate;

    /** 主后端服务的基础URL */
    @Value("${hjy.ai.hjy-community.base-url:http://localhost:8080}")
    private String baseUrl;

    /** 管理员用户名，用于自动登录获取 Token */
    @Value("${hjy.ai.hjy-community.admin-username:admin}")
    private String adminUsername;

    /** 管理员密码 */
    @Value("${hjy.ai.hjy-community.admin-password:}")
    private String adminPassword;

    /** 固定 API Token（如配置则优先使用，跳过自动登录） */
    @Value("${hjy.ai.hjy-community.api-token:}")
    private String apiToken;

    /** 缓存的管理员 JWT Token */
    private volatile String token;
    /** Token 过期时间戳 */
    private volatile long tokenExpireTime;

    /** JSON 解析器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Token 有效时长：25 分钟（服务端 30 分钟过期，提前刷新） */
    private static final long TOKEN_VALID_TIME = 25 * 60 * 1000L;

    /**
     * 获取认证 Token
     * <p>
     * 优先返回配置的固定 api-token；其次使用缓存的 token（未过期时）；
     * 否则通过 /aiLogin 接口自动登录获取新 token。
     *
     * @return JWT Token 字符串
     */
    public synchronized String getToken() {
        // 如果配置了固定的 api-token，直接返回
        if (apiToken != null && !apiToken.isEmpty()) {
            return apiToken;
        }

        // 否则使用缓存的 token（未过期时复用）
        if (token != null && System.currentTimeMillis() < tokenExpireTime) {
            return token;
        }

        try {
            String url = baseUrl + "/aiLogin";

            Map<String, Object> loginBody = new HashMap<>();
            loginBody.put("username", adminUsername);
            loginBody.put("password", adminPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(loginBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                this.token = root.path("token").asText();
                this.tokenExpireTime = System.currentTimeMillis() + TOKEN_VALID_TIME;
                log.info("获取Token成功");
                return token;
            }
        } catch (Exception e) {
            log.error("获取Token失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 发送 GET 请求（无查询参数）
     *
     * @param path 请求路径
     * @return 响应体 JSON 字符串
     */
    public String get(String path) {
        return get(path, new HashMap<>());
    }

    /**
     * 发送 GET 请求（带查询参数）
     *
     * @param path   请求路径
     * @param params 查询参数 Map
     * @return 响应体 JSON 字符串
     */
    public String get(String path, Map<String, Object> params) {
        try {
            String token = getToken();
            if (token == null) {
                return buildErrorJson(500, "无法获取认证Token");
            }

            String url;
            if (params.isEmpty()) {
                url = baseUrl + path;
            } else {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
                params.forEach((k, v) -> builder.queryParam(k, v));
                url = builder.toUriString();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("GET请求失败: path: {}, error: {}", path, e.getMessage());
            return buildErrorJson(500, "请求失败，请稍后重试");
        }
    }

    /**
     * 发送 POST 请求
     *
     * @param path 请求路径
     * @param body 请求体 Map（自动转为 JSON）
     * @return 响应体 JSON 字符串
     */
    public String post(String path, Map<String, Object> body) {
        try {
            String token = getToken();
            if (token == null) {
                return buildErrorJson(500, "无法获取认证Token");
            }

            String url = baseUrl + path;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("POST请求失败: path: {}, error: {}", path, e.getMessage());
            return buildErrorJson(500, "请求失败，请稍后重试");
        }
    }

    /**
     * 发送 PUT 请求
     *
     * @param path 请求路径
     * @param body 请求体 Map（自动转为 JSON）
     * @return 响应体 JSON 字符串
     */
    public String put(String path, Map<String, Object> body) {
        try {
            String token = getToken();
            if (token == null) {
                return buildErrorJson(500, "无法获取认证Token");
            }

            String url = baseUrl + path;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("PUT请求失败: path: {}, error: {}", path, e.getMessage());
            return buildErrorJson(500, "请求失败，请稍后重试");
        }
    }

    /**
     * 构建错误 JSON 响应（用于请求失败时返回统一格式）
     */
    private String buildErrorJson(int code, String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("code", code);
            error.put("msg", message);
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            return "{\"code\":500,\"msg\":\"请求失败\"}";
        }
    }
}