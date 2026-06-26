package org.example.redis.pubsub.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

public class LettuceClientProvider {
    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, String> connection;

    static {
        RedisURI uri = RedisURI.builder()
                .withHost(RedisConfig.getHost())
                .withPort(RedisConfig.getPort())
                .withDatabase(RedisConfig.getDatabase())
                .withTimeout(java.time.Duration.ofMillis(RedisConfig.getTimeout()))
                .build();

        String password = RedisConfig.getPassword();
        if (password != null && !password.isEmpty()) {
            uri.setPassword(password);
        }

        redisClient = RedisClient.create(uri);
        connection = redisClient.connect();
    }

    public static StatefulRedisConnection<String, String> getConnection() {
        if (connection == null || !connection.isOpen()) {
            connection = redisClient.connect();
        }
        return connection;
    }

    public static void close() {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }
}

