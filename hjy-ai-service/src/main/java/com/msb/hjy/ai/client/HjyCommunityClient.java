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

@Slf4j
@Component
public class HjyCommunityClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${hjy.ai.hjy-community.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${hjy.ai.hjy-community.admin-username:admin}")
    private String adminUsername;

    @Value("${hjy.ai.hjy-community.admin-password:}")
    private String adminPassword;

    @Value("${hjy.ai.hjy-community.api-token:}")
    private String apiToken;

    private volatile String token;
    private volatile long tokenExpireTime;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long TOKEN_VALID_TIME = 25 * 60 * 1000L;

    public synchronized String getToken() {
        // 如果配置了固定的api-token，直接返回
        if (apiToken != null && !apiToken.isEmpty()) {
            return apiToken;
        }

        // 否则使用缓存的token
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

    public String get(String path) {
        return get(path, new HashMap<>());
    }

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