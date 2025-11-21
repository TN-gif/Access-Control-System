package com.rbac.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置工具类 - 负责读取配置文件
 */
public class ConfigUtil {
    
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigUtil.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("无法找到 config.properties 文件");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("加载配置文件失败", e);
        }
    }
    
    /**
     * 获取字符串配置
     */
    public static String getString(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取字符串配置，提供默认值
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数配置
     */
    public static int getInt(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("配置项不存在: " + key);
        }
        return Integer.parseInt(value);
    }
    
    /**
     * 获取整数配置，提供默认值
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
