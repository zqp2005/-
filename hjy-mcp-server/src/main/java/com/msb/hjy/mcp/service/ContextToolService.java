package com.msb.hjy.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 上下文工具服务 —— 经 MCP 协议暴露给 AI 服务的环境感知工具
 * <p>
 * 提供两个工具（供 hjy-ai-service 中的大模型调用）：
 * <ul>
 *     <li>{@link #queryLoginLocation(String)}：根据登录日志定位用户所在城市。
 *         优先取最近一次登录 IP 调高德 IP 定位；IP 为内网/空时回退使用社区注册地址。</li>
 *     <li>{@link #queryWeather(String)}：调高德天气 API 查询城市实时天气。</li>
 * </ul>
 * 高德 Key 通过配置 hjy.mcp.amap-key 注入，未配置时工具返回明确提示而非抛异常。
 */
@Slf4j
@Service
public class ContextToolService {

    /** 高德 Web 服务 Key（application.yml 中由 AMAP_KEY 环境变量/local 配置提供） */
    @Value("${hjy.mcp.amap-key:}")
    private String amapKey;

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate amapRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContextToolService(JdbcTemplate jdbcTemplate, RestTemplate amapRestTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.amapRestTemplate = amapRestTemplate;
    }

    /**
     * 查询用户最近一次登录的 IP 归属地（即用户大致所在城市）
     * <p>
     * 流程：查 sys_logininfor 最近一条登录记录 →
     * 内网/空 IP 则回退查询 hjy_community 的社区注册地址 →
     * 公网 IP 则调高德 IP 定位接口解析省份/城市。
     *
     * @param userName 用户名（如 admin）
     * @return 给 AI 的中文描述文本，含数据来源说明
     */
    @Tool(name = "query_login_location",
            description = "查询当前用户登录地址/所在城市。当用户问'我在哪'、'我的登录位置'、'我登录的IP是哪里'、'根据登录记录判断用户在哪个城市'时调用")
    public String queryLoginLocation(
            @ToolParam(description = "要查询的用户名，例如 admin；应传当前登录用户名") String userName) {
        log.info("MCP工具 query_login_location 被调用 - userName: {}", userName);

        if (userName == null || userName.isBlank()) {
            return "请提供要查询的用户名（userName），例如当前登录用户名。";
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT ipaddr, login_location FROM sys_logininfor WHERE user_name = ? ORDER BY login_time DESC LIMIT 1",
                    userName);
            if (rows.isEmpty()) {
                return "未查到用户「" + userName + "」的登录记录。";
            }

            String ip = asText(rows.get(0).get("ipaddr"));
            String dbLocation = asText(rows.get(0).get("login_location"));

            // 内网/空 IP：本地运行场景——本服务与用户同机，直接让高德定位"请求发起方"
            // （即本机的公网出口IP，反映当前登录用户的真实所在地），失败再回退社区注册地址
            if (isInternalIp(ip)) {
                if (amapKey != null && !amapKey.isBlank()) {
                    try {
                        String selfBody = amapRestTemplate.getForObject(
                                "https://restapi.amap.com/v3/ip?key={key}", String.class, amapKey);
                        JsonNode selfRoot = objectMapper.readTree(selfBody);
                        if ("1".equals(selfRoot.path("status").asText())) {
                            String province = selfRoot.path("province").asText("");
                            String city = selfRoot.path("city").asText("");
                            String location = city.isBlank() || city.equals(province)
                                    ? province : province + city;
                            if (!location.isBlank()) {
                                return "用户「" + userName + "」本次登录来自本机（内网IP " + displayIp(ip)
                                        + "），当前网络出口所在地: " + location + "（数据来源：高德IP定位）";
                            }
                        }
                    } catch (Exception e) {
                        log.warn("本机公网出口定位失败: {}", e.getMessage());
                    }
                }
                return "用户「" + userName + "」最近一次登录IP为 " + displayIp(ip)
                        + "（本地内网环境，公网定位失败），使用社区注册地址: " + communityAddress();
            }

            // 公网 IP：需要高德 Key 才能定位
            if (amapKey == null || amapKey.isBlank()) {
                return "高德Key未配置（请设置 hjy.mcp.amap-key 或 AMAP_KEY 环境变量），无法解析公网IP归属地。"
                        + "该用户最近一次登录IP: " + ip
                        + (dbLocation.isEmpty() || "unknown".equalsIgnoreCase(dbLocation)
                        ? "" : "，登录日志记录的归属地: " + dbLocation);
            }

            String body = amapRestTemplate.getForObject(
                    "https://restapi.amap.com/v3/ip?ip={ip}&key={key}", String.class, ip, amapKey);
            JsonNode root = objectMapper.readTree(body);
            if ("1".equals(root.path("status").asText())) {
                String province = root.path("province").asText("");
                String city = root.path("city").asText("");
                String location = city.isBlank() || city.equals(province)
                        ? province : province + city;
                if (!location.isBlank()) {
                    return "该用户「" + userName + "」最近一次登录IP: " + ip
                            + "，归属地: " + location + "（数据来源：高德IP定位）";
                }
            }
            log.warn("高德IP定位失败 - ip: {}, status: {}", ip, root.path("status").asText());

            // 高德定位失败：优先登录日志里记录的归属地，其次社区注册地址
            if (!dbLocation.isEmpty() && !"unknown".equalsIgnoreCase(dbLocation)) {
                return "该用户「" + userName + "」最近一次登录IP: " + ip
                        + "，归属地: " + dbLocation + "（数据来源：登录日志记录）";
            }
            return "该用户「" + userName + "」最近一次登录IP: " + ip
                    + "，IP归属地解析失败，使用社区注册地址: " + communityAddress();

        } catch (Exception e) {
            log.error("查询登录地址失败 - userName: {}", userName, e);
            return "查询用户「" + userName + "」登录地址失败：" + e.getMessage();
        }
    }

    /**
     * 查询城市实时天气（高德天气 API，extensions=base 实况）
     *
     * @param city 城市名称（直接传中文如"北京"，或高德 adcode 如"110000"）
     * @return 给 AI 的中文描述文本；未查到时明确说明
     */
    @Tool(name = "query_weather",
            description = "查询某城市实时天气。当用户问'今天天气怎么样'、'XX市天气'、'下雨吗'、'温度多少'时调用")
    public String queryWeather(
            @ToolParam(description = "城市名称（中文如'北京'）或高德adcode（如'110000'）") String city) {
        log.info("MCP工具 query_weather 被调用 - city: {}", city);

        if (city == null || city.isBlank()) {
            return "请提供要查询的城市名称（city），例如'北京'。";
        }
        if (amapKey == null || amapKey.isBlank()) {
            return "高德Key未配置（请设置 hjy.mcp.amap-key 或 AMAP_KEY 环境变量），无法查询「" + city + "」天气。";
        }

        try {
            String body = amapRestTemplate.getForObject(
                    "https://restapi.amap.com/v3/weather/weatherInfo?city={city}&key={key}&extensions=base",
                    String.class, city, amapKey);
            JsonNode root = objectMapper.readTree(body);
            if (!"1".equals(root.path("status").asText())) {
                log.warn("高德天气接口返回异常 - city: {}, body: {}", city, body);
                return "未查到「" + city + "」天气（天气服务返回异常）。";
            }

            JsonNode lives = root.path("lives");
            if (!lives.isArray() || lives.isEmpty()) {
                return "未查到「" + city + "」天气。";
            }

            JsonNode live = lives.get(0);
            StringBuilder sb = new StringBuilder();
            sb.append("【").append(city).append(" 天气实况】\n");
            if (!live.path("weather").asText("").isBlank()) {
                sb.append("  天气：").append(live.path("weather").asText()).append("\n");
            }
            if (!live.path("temperature").asText("").isBlank()) {
                sb.append("  温度：").append(live.path("temperature").asText()).append("℃\n");
            }
            if (!live.path("winddirection").asText("").isBlank()) {
                sb.append("  风向：").append(live.path("winddirection").asText()).append("\n");
            }
            if (!live.path("windpower").asText("").isBlank()) {
                sb.append("  风力：").append(live.path("windpower").asText()).append("级\n");
            }
            if (!live.path("humidity").asText("").isBlank()) {
                sb.append("  湿度：").append(live.path("humidity").asText()).append("%\n");
            }
            if (!live.path("reporttime").asText("").isBlank()) {
                sb.append("  发布时间：").append(live.path("reporttime").asText()).append("\n");
            }
            sb.append("（数据来源：高德天气）");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询天气失败 - city: {}", city, e);
            return "查询「" + city + "」天气失败：" + e.getMessage();
        }
    }

    /**
     * 查询社区注册地址（hjy_community 第一条记录的名称+详细地址）
     */
    private String communityAddress() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT community_name, community_detailed_address FROM hjy_community LIMIT 1");
            if (!rows.isEmpty()) {
                String name = asText(rows.get(0).get("community_name"));
                String address = asText(rows.get(0).get("community_detailed_address"));
                if (!address.isBlank()) {
                    return name.isBlank() ? address : name + "（" + address + "）";
                }
                if (!name.isBlank()) {
                    return name;
                }
            }
        } catch (Exception e) {
            log.warn("查询社区注册地址失败: {}", e.getMessage());
        }
        return "合家云社区（社区注册地址未配置）";
    }

    /**
     * 判断是否内网/本地/无效 IP（127.*、192.168.*、10.*、172.16-31.*、localhost、IPv6 回环、空、unknown）
     */
    private boolean isInternalIp(String ip) {
        if (ip == null) {
            return true;
        }
        String v = ip.trim().toLowerCase();
        if (v.isEmpty() || "unknown".equals(v) || "localhost".equals(v) || "内网ip".equals(v) || "内网 ip".equals(v)) {
            return true;
        }
        // IPv6 回环/本地
        if (v.equals("::1") || v.equals("0:0:0:0:0:0:0:1") || v.startsWith("fe80:") || v.startsWith("fc") || v.startsWith("fd")) {
            return true;
        }
        // IPv4 私有网段
        return v.startsWith("127.") || v.startsWith("192.168.") || v.startsWith("10.")
                || v.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*");
    }

    /** IP 为空时的展示文案 */
    private String displayIp(String ip) {
        return (ip == null || ip.isBlank()) ? "无记录" : ip.trim();
    }

    /** 数据库字段值安全转文本 */
    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
