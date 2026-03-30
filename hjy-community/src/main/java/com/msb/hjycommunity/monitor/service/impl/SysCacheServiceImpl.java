package com.msb.hjycommunity.monitor.service.impl;

import com.msb.hjycommunity.monitor.service.SysCacheService;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 缓存监控Service实现
 */
@Service
public class SysCacheServiceImpl implements SysCacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<String, Object> getCache() {
        Map<String, Object> result = new HashMap<>();
        
        // Redis信息
        Map<String, Object> info = new HashMap<>();
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties infoProperties = connection.info();
            connection.close();
            
            if (infoProperties != null) {
                info.put("redis_version", infoProperties.getProperty("redis_version", "unknown"));
                info.put("redis_mode", infoProperties.getProperty("redis_mode", "unknown"));
                info.put("tcp_port", infoProperties.getProperty("tcp_port", "6379"));
                info.put("connected_clients", infoProperties.getProperty("connected_clients", "0"));
                info.put("uptime_in_days", infoProperties.getProperty("uptime_in_days", "0"));
                info.put("used_memory_human", infoProperties.getProperty("used_memory_human", "0"));
                info.put("used_cpu_user_children", infoProperties.getProperty("used_cpu_user_children", "0"));
                info.put("maxmemory_human", infoProperties.getProperty("maxmemory_human", "0"));
                info.put("aof_enabled", infoProperties.getProperty("aof_enabled", "0"));
                info.put("rdb_last_bgsave_status", infoProperties.getProperty("rdb_last_bgsave_status", "unknown"));
                info.put("instantaneous_input_kbps", infoProperties.getProperty("instantaneous_input_kbps", "0"));
                info.put("instantaneous_output_kbps", infoProperties.getProperty("instantaneous_output_kbps", "0"));
            }
        } catch (Exception e) {
            info.put("redis_version", "无法获取");
        }
        result.put("info", info);
        
        // 键数量
        try {
            Set<String> keys = redisTemplate.keys("*");
            result.put("dbSize", keys != null ? keys.size() : 0);
        } catch (Exception e) {
            result.put("dbSize", 0);
        }
        
        // 命令统计
        List<Map<String, String>> commandStats = new ArrayList<>();
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties commandStatsProperties = connection.info("commandstats");
            connection.close();
            
            if (commandStatsProperties != null) {
                for (String key : commandStatsProperties.stringPropertyNames()) {
                    if (key.startsWith("cmdstat_")) {
                        String value = commandStatsProperties.getProperty(key);
                        String name = key.replace("cmdstat_", "");
                        Map<String, String> stat = new HashMap<>();
                        stat.put("name", name);
                        stat.put("value", value);
                        commandStats.add(stat);
                        if (commandStats.size() >= 10) break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        result.put("commandStats", commandStats);
        
        return result;
    }
}
