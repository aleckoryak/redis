Cache Stampede (Thundering Herd / Пробитие кэша): Кэш тяжелого «горячего» запроса (например, главная страница маркетплейса) исчез. 10 000 параллельных потоков одновременно видят Cache Miss и одновременно начинают выполнять один и тот же тяжелый SQL-запрос в БД.

Решение: Использование распределенных блокировок (Mutex/Redlock) в коде приложения: только первый поток идет в БД, остальные ждут, пока он обновит кэш. 

Для решения проблемы Cache Stampede (Thundering Herd) с помощью распределенных блокировок нам нужно реализовать следующую логику:

Поток пытается получить данные из кэша Redis.

Если данных нет (Cache Miss), поток не бежит сразу в базу данных. Вместо этого он пытается взять распределенную блокировку (например, через Redisson) для этого конкретного ключа.

Первый поток, который успешно захватил блокировку, отправляется в тяжелую БД, забирает данные, сохраняет их в Redis (обновляет кэш) и затем отпускает блокировку.

Все остальные 9 999 потоков, которые не смогли взять блокировку, послушно ждут (блокируются на методе или засыпают на короткое время), а после освобождения блокировки или по таймауту — снова проверяют кэш. К этому моменту первый поток уже запишет туда свежие данные, и они прочитают их из Redis, не нагружая БД.

Вот подробный Java-пример реализации этого паттерна с использованием Spring Boot и Redisson:

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplacePageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final HeavyDatabaseRepository databaseRepository;

    private static final String CACHE_KEY = "marketplace:homepage:data";
    private static final String LOCK_KEY = "lock:marketplace:homepage";

    public String getHomepageData() {
        // 1. Быстрая попытка на чтение из кэша
        String cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedData != null) {
            return cachedData; // Cache Hit! (Сюда провалятся 99% запросов)
        }

        // 2. Cache Miss — Начало защиты от Cache Stampede
        log.warn("Cache Miss для главной страницы. Пытаемся захватить блокировку...");
        RLock lock = redissonClient.getLock(LOCK_KEY);
        
        try {
            // Пытаемся взять блокировку. 
            // waitTime = 5 секунд (сколько потоки готовы ждать своей очереди)
            // leaseTime = 10 секунд (время жизни блокировки, если поток «умрет»)
            boolean isLockAcquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (isLockAcquired) {
                log.info("Блокировка успешно взята. Текущий поток идет в БД.");

                // КРИТИЧЕСКИЙ ШАГ: Double-Check (Двойная проверка)
                // Пока мы ждали блокировку, поток, шедший перед нами, мог уже обновить кэш.
                cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
                if (cachedData != null) {
                    log.info("Данные уже появились в кэше (Double-Check сработал).");
                    return cachedData;
                }

                // 3. Единственный поток выполняет тяжелый запрос в БД
                String freshDataFromDb = databaseRepository.getHeavyHomepageDataFromPostgres();

                // 4. Записываем данные в кэш (например, на 10 минут)
                redisTemplate.opsForValue().set(CACHE_KEY, freshDataFromDb, Duration.ofMinutes(10));

                return freshDataFromDb;
            } else {
                // Сюда попадают потоки, которые не успели взять блокировку за waitTime.
                // Это значит, что система перегружена, либо первый поток слишком долго ходит в БД.
                log.error("Не удалось дождаться блокировки. Пробуем прочитать кэш в последний раз.");
                String finalAttemptData = redisTemplate.opsForValue().get(CACHE_KEY);
                if (finalAttemptData != null) {
                    return finalAttemptData;
                }
                throw new RuntimeException("Система перегружена (Таймаут ожидания кэша)");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Процесс получения данных был прерван", e);
        } finally {
            // Освобождаем блокировку, только если её держит текущий поток
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Блокировка освобождена.");
            }
        }
    }
}
```