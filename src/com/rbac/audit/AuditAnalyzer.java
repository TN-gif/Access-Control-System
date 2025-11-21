package com.rbac.audit;

import com.rbac.util.ConfigUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 智能审计分析器 - 分析审计日志中的异常行为
 */
public class AuditAnalyzer {
    
    private final Path logPath;
    private final int thresholdPerHour;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    public AuditAnalyzer() {
        String logPathStr = ConfigUtil.getString("audit.log.path", "logs/audit.log");
        this.logPath = Paths.get(logPathStr);
        this.thresholdPerHour = ConfigUtil.getInt("audit.threshold.login.fail.per_hour", 5);
    }
    
    /**
     * 分析审计日志，返回警告信息列表
     */
    public List<String> analyze() {
        List<String> warnings = new ArrayList<>();
        
        if (!Files.exists(logPath)) {
            warnings.add("审计日志文件不存在: " + logPath);
            return warnings;
        }
        
        try (Stream<String> lines = Files.lines(logPath)) {
            // 使用流式处理直接统计，避免将所有日志加载到内存
            Map<String, Map<LocalDateTime, Long>> stats = lines
                    .map(this::parseLine)
                    .filter(Objects::nonNull)
                    .filter(e -> "AUDIT_FAIL".equals(e.getLevel())) // 只关心失败日志
                    .filter(e -> "LOGIN".equals(e.getAction()))     // 只关心登录操作
                    .collect(Collectors.groupingBy(
                            LogEntry::getUser,
                            Collectors.groupingBy(
                                    e -> e.getTimestamp().truncatedTo(ChronoUnit.HOURS),
                                    Collectors.counting()
                            )
                    ));
            
            // 检查是否超过阈值
            stats.forEach((user, hourMap) -> {
                hourMap.forEach((hour, count) -> {
                    if (count >= thresholdPerHour) {
                        warnings.add(String.format(
                                "高风险：用户[%s]在[%s]这一小时内登录失败%d次，可能存在暴力破解或异常登录",
                                user, hour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00")), count
                        ));
                    }
                });
            });
            
        } catch (IOException e) {
            warnings.add("读取审计日志失败: " + e.getMessage());
        }
        
        return warnings;
    }
    
    /**
     * 解析日志行
     * 格式: 2025-11-18T10:00:00 [AUDIT_FAIL] user=alice action=LOGIN target=alice msg=密码错误 result=FAIL
     */
    private LogEntry parseLine(String line) {
        try {
            if (line == null || line.trim().isEmpty()) {
                return null;
            }
            
            // 提取时间戳
            int firstSpace = line.indexOf(' ');
            if (firstSpace < 0) {
                return null;
            }
            String timeStr = line.substring(0, firstSpace);
            LocalDateTime timestamp = LocalDateTime.parse(timeStr, TIME_FORMATTER);
            
            // 提取日志级别
            int levelStart = line.indexOf('[');
            int levelEnd = line.indexOf(']');
            if (levelStart < 0 || levelEnd < 0) {
                return null;
            }
            String level = line.substring(levelStart + 1, levelEnd);
            
            // 提取字段
            String content = line.substring(levelEnd + 1).trim();
            Map<String, String> fields = new HashMap<>();
            
            String[] parts = content.split(" ");
            for (String part : parts) {
                int eqIndex = part.indexOf('=');
                if (eqIndex > 0) {
                    String key = part.substring(0, eqIndex);
                    String value = part.substring(eqIndex + 1);
                    fields.put(key, value);
                }
            }
            
            return new LogEntry(timestamp, level, 
                    fields.getOrDefault("user", ""),
                    fields.getOrDefault("action", ""),
                    fields.getOrDefault("target", ""),
                    fields.getOrDefault("msg", ""),
                    fields.getOrDefault("result", ""));
        } catch (Exception e) {
            // 解析失败，忽略该行
            return null;
        }
    }
    
    /**
     * 日志条目类
     */
    private static class LogEntry {
        private final LocalDateTime timestamp;
        private final String level;
        private final String user;
        private final String action;
        private final String target;
        private final String message;
        private final String result;
        
        public LogEntry(LocalDateTime timestamp, String level, String user, 
                       String action, String target, String message, String result) {
            this.timestamp = timestamp;
            this.level = level;
            this.user = user;
            this.action = action;
            this.target = target;
            this.message = message;
            this.result = result;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getLevel() {
            return level;
        }
        
        public String getUser() {
            return user;
        }
        
        public String getAction() {
            return action;
        }
        
        public String getTarget() {
            return target;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getResult() {
            return result;
        }
    }
}
