package com.msb.hjy.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    @Value("${hjy.ai.hjy-community.admin-password:admin123}")
    private String adminPassword;

    private String token;
    private long tokenExpireTime;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long TOKEN_VALID_TIME = 25 * 60 * 1000L;

    public String getToken() {
        if (token != null && System.currentTimeMillis() < tokenExpireTime) {
            return token;
        }

        try {
            String url = baseUrl + "/login";
            
            Map<String, Object> loginBody = new HashMap<>();
            loginBody.put("username", adminUsername);
            loginBody.put("password", adminPassword);
            loginBody.put("code", "");
            loginBody.put("uuid", "");

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
                return "{\"code\":500,\"msg\":\"无法获取认证Token\"}";
            }

            String url = baseUrl + path;
            if (!params.isEmpty()) {
                StringBuilder sb = new StringBuilder(url).append("?");
                params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
                url = sb.toString();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("GET请求失败: {}, path: {}", e.getMessage(), path);
            return "{\"code\":500,\"msg\":\"请求失败: " + e.getMessage() + "\"}";
        }
    }

    public String post(String path, Map<String, Object> body) {
        try {
            String token = getToken();
            if (token == null) {
                return "{\"code\":500,\"msg\":\"无法获取认证Token\"}";
            }

            String url = baseUrl + path;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("POST请求失败: {}, path: {}", e.getMessage(), path);
            return "{\"code\":500,\"msg\":\"请求失败: " + e.getMessage() + "\"}";
        }
    }
}
