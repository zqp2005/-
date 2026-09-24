package com.msb.hjy.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

/** 只透传调用者令牌；禁止共享管理员身份或认证失败后切换身份重试。 */
@Component
public class HjyCommunityClient {
    @Autowired private RestTemplate restTemplate;
    @Value("${hjy.ai.hjy-community.base-url:http://localhost:8080}") private String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public String get(String path) { return get(path, Map.of()); }

    public String get(String path, Map<String, Object> params) {
        String authorization = CallerContext.current();
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("缺少调用者身份");
        }
        if (!path.startsWith("/") || path.startsWith("//") || path.contains("://")) {
            throw new IllegalArgumentException("非法业务路径");
        }
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
        if (params != null) params.forEach((key, value) -> uri.queryParam(key, value));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        ResponseEntity<String> response = restTemplate.exchange(uri.build().encode().toUri(),
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            JsonNode body = mapper.readTree(response.getBody());
            if (!response.getStatusCode().is2xxSuccessful() || body == null || body.path("code").asInt(-1) != 200) {
                throw new IllegalStateException("业务查询被拒绝或失败，不能当作无数据");
            }
            return response.getBody();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("业务响应格式错误", e);
        }
    }

    public String post(String path, Map<String, Object> body) {
        return "{\"code\":403,\"msg\":\"请到业务页面确认并提交，AI写操作尚未开放\"}";
    }
    public String put(String path, Map<String, Object> body) { return post(path, body); }
}
