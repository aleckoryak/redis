

## using Redisson distributed lock

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketBookingService {

    private final RedissonClient redissonClient;

    public void bookSeat(Long seatId) {
        String lockKey = "lock:seat:" + seatId;
        // Получаем объект блокировки (в Redis ничего пока не отправляется)
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Пытаемся захватить блокировку:
            // 10 — время ожидания захвата блокировки (wait time) в секундах
            // 30 — время жизни блокировки (lease time) в секундах
            // TimeUnit.SECONDS — единицы времени
            boolean isLockAcquired = lock.tryLock(10, 30, TimeUnit.SECONDS);

            if (isLockAcquired) {
                log.info("Блокировка успешно взята для места: {}", seatId);
                
                // КРИТИЧЕСКАЯ СЕКЦИЯ: Выполняем бизнес-логику (запись в БД)
                executeBookingLogic(seatId);
                
            } else {
                log.warn("Не удалось взять блокировку. Место {} сейчас занято кем-то другим", seatId);
                throw new BookingException("Место уже бронируется, попробуйте позже.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookingException("Процесс бронирования был прерван", e);
        } finally {
            // ВАЖНО: Освобождаем блокировку только если текущий поток является её владельцем
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Блокировка для места {} успешно снята", seatId);
            }
        }
    }

    private void executeBookingLogic(Long seatId) {
        // Имитация тяжелой работы с БД
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }
}
```


## redis template

```java
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomDistributedLock {

    private final StringRedisTemplate redisTemplate;

    // Lua-скрипт для атомарного удаления ключа (проверяет совпадение значения)
    private static final String RELEASE_LOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * Пытается захватить блокировку
     * @return Возвращает уникальный токен блокировки (успех), либо null (отказ)
     */
    public String acquireLock(String lockKey, long expireTimeInSeconds) {
        // Генерируем уникальное значение для текущего потока/инстанса
        String lockValue = UUID.randomUUID().toString();

        // Эквивалент команды: SET key value NX EX expireTime
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                lockKey, 
                lockValue, 
                expireTimeInSeconds, 
                TimeUnit.SECONDS
        );

        return Boolean.TRUE.equals(success) ? lockValue : null;
    }

    /**
     * Безопасно освобождает блокировку через Lua-скрипт
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) return false;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RELEASE_LOCK_LUA_SCRIPT);
        redisScript.setResultType(Long.class);

        // Выполняем скрипт атомарно на стороне Redis
        Long result = redisTemplate.execute(
                redisScript, 
                Collections.singletonList(lockKey), 
                lockValue
        );

        return Long.valueOf(1L).equals(result);
    }
}

```


```java 
public void processOrder(Long orderId) {
    String lockKey = "lock:order:" + orderId;
    
    // Пытаемся взять блокировку на 15 секунд
    String lockToken = customLock.acquireLock(lockKey, 15);
    
    if (lockToken != null) { // Блокировка успешно взята
        try {
            // Бизнес-логика обработки заказа
            orderRepository.ship(orderId);
        } finally {
            // Освобождаем, передавая ключ и наш токен
            customLock.releaseLock(lockKey, lockToken);
        }
    } else {
        // Не удалось взять блокировку — кто-то уже обрабатывает этот заказ
        throw new RuntimeException("Заказ уже обрабатывается другим процессом");
    }
}
```
