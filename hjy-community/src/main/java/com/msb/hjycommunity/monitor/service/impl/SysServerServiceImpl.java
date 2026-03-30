package com.msb.hjycommunity.monitor.service.impl;

import com.msb.hjycommunity.monitor.service.SysServerService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 服务器监控Service实现
 */
@Service
public class SysServerServiceImpl implements SysServerService {

    @Override
    public Map<String, Object> getServerInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // CPU信息
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("cpuNum", Runtime.getRuntime().availableProcessors());
        cpu.put("used", 0.0);
        cpu.put("sys", 0.0);
        cpu.put("free", 100.0);
        result.put("cpu", cpu);
        
        // 内存信息
        Map<String, Object> mem = new HashMap<>();
        long total = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024);
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024 * 1024);
        long free = total - used;
        mem.put("total", total);
        mem.put("used", used);
        mem.put("free", free);
        mem.put("usage", Math.round(used * 100.0 / total * 100) / 100.0);
        result.put("mem", mem);
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("name", ManagementFactory.getRuntimeMXBean().getVmName());
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("total", runtime.totalMemory() / (1024 * 1024));
        jvm.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        jvm.put("free", runtime.freeMemory() / (1024 * 1024));
        jvm.put("usage", Math.round((runtime.totalMemory() - runtime.freeMemory()) * 100.0 / runtime.totalMemory() * 100) / 100.0);
        jvm.put("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime())));
        jvm.put("runTime", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        jvm.put("home", System.getProperty("java.home"));
        result.put("jvm", jvm);
        
        // 系统信息
        Map<String, Object> sys = new HashMap<>();
        sys.put("computerName", System.getProperty("user.name"));
        sys.put("computerIp", "127.0.0.1");
        sys.put("osName", System.getProperty("os.name"));
        sys.put("osArch", System.getProperty("os.arch"));
        sys.put("userDir", System.getProperty("user.dir"));
        result.put("sys", sys);
        
        // 磁盘信息
        List<Map<String, Object>> sysFiles = new ArrayList<>();
        Map<String, Object> file = new HashMap<>();
        file.put("dirName", System.getProperty("user.dir"));
        file.put("sysTypeName", "local");
        file.put("typeName", "local");
        file.put("total", total + "GB");
        file.put("free", free + "GB");
        file.put("used", used + "GB");
        file.put("usage", Math.round(used * 100.0 / total * 100) / 100.0);
        sysFiles.add(file);
        result.put("sysFiles", sysFiles);
        
        return result;
    }
    
    private String formatUptime(long uptime) {
        long days = uptime / (1000 * 60 * 60 * 24);
        long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        return days + "天" + hours + "小时" + minutes + "分钟";
    }
}
