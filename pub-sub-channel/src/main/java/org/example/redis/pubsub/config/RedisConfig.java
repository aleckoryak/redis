package org.example.redis.pubsub.config;

import java.io.IOException;
import java.util.Properties;

public class RedisConfig {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(RedisConfig.class.getClassLoader()
                    .getResourceAsStream("redis.properties"));
        } catch (IOException e) {
            System.err.println("Error loading redis.properties: " + e.getMessage());
        }
    }

    public static String getHost() {
        return properties.getProperty("redis.host", "localhost");
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("redis.port", "6379"));
    }

    public static int getDatabase() {
        return Integer.parseInt(properties.getProperty("redis.database", "0"));
    }

    public static String getPassword() {
        String password = properties.getProperty("redis.password", "");
        return password.isEmpty() ? null : password;
    }

    public static int getTimeout() {
        return Integer.parseInt(properties.getProperty("redis.timeout", "2000"));
    }
}

