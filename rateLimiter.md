## 1. Sliding Window Rate Limiter (based on Sorted Set / ZSET)
Every request adds a unique element to the Sorted Set, where the Score is the current timestamp in milliseconds. Then we remove all elements older than our window and count how many are left.

```java
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Проверяет, превышен ли лимит запросов.
     * @param clientId Идентификатор клиента (IP, API Key, User ID)
     * @param limit Максимальное количество запросов
     * @param windowInSeconds Размер окна в секундах (например, 60)
     * @return true если запрос разрешен, false если заблокирован
     */
    public boolean isAllowed(String clientId, int limit, long windowInSeconds) {
        String key = "rate:sliding:" + clientId;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowInSeconds * 1000);

        // Используем UUID в качестве значения, чтобы каждый запрос был уникальным в ZSET
        String requestId = UUID.randomUUID().toString();

        // 1. Удаляем все старые запросы, которые вышли за рамки текущего временного окна
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // 2. Получаем текущее количество запросов в окне
        Long currentRequestCount = redisTemplate.opsForZSet().zCard(key);

        if (currentRequestCount != null && currentRequestCount >= limit) {
            // Лимит превышен, запрос отклонен
            return false;
        }

        // 3. Если лимит не превышен, добавляем текущий запрос в ZSET
        redisTemplate.opsForZSet().add(key, requestId, now);
        
        // Обновляем TTL ключа, чтобы он не висел в памяти вечно, если клиент перестанет слать запросы
        redisTemplate.expire(key, java.time.Duration.ofSeconds(windowInSeconds));

        return true;
    }
}
```



## Token Bucket Rate Limiter (using Redisson)
```java
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiter {

    private final RedissonClient redissonClient;

    /**
     * Проверяет доступность токена по алгоритму Token Bucket
     */
    public boolean tryAcquire(String clientId, int maxTokens, long intervalInSeconds) {
        // Получаем объект рейд-лимитера по ключу клиента
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rate:bucket:" + clientId);

        // Инициализируем лимитер, если он еще не создан в Redis:
        // RateType.OVERALL — лимит общий для всех инстансов приложения
        // maxTokens — сколько токенов в ведре
        // intervalInSeconds — за какое время ведро полностью наполняется
        rateLimiter.trySetRate(RateType.OVERALL, maxTokens, intervalInSeconds, RateIntervalUnit.SECONDS);

        // Пытаемся забрать 1 токен. Возвращает true, если токен взят, или false, если ведро пустое.
        // Метод не блокирует поток, а возвращает результат мгновенно.
        return rateLimiter.tryAcquire(1);
    }
}

```