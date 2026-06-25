## Cache-Aside (Lazy Loading)

```java
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserServiceCacheAside {

    private final UserRepository userRepository;
    private final RedisTemplate<String, User> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:";

    public User getUserById(Long id) {
        String key = CACHE_KEY_PREFIX + id;

        // 1. Пытаемся получить данные из Redis
        User cachedUser = redisTemplate.opsForValue().get(key);
        if (cachedUser != null) {
            return cachedUser; // Cache Hit!
        }

        // 2. Cache Miss — идем в основную БД
        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Добавляем TTL Jitter (шум), чтобы избежать Лавины Кэша (Cache Avalanche)
        // Базовый TTL 30 минут + случайные от 1 до 5 минут
        int randomMinutes = ThreadLocalRandom.current().nextInt(1, 6);
        Duration ttl = Duration.ofMinutes(30).plus(Duration.ofMinutes(randomMinutes));

        // 4. Сохраняем в Redis
        redisTemplate.opsForValue().set(key, userFromDb, ttl);

        return userFromDb;
    }
}
```

## Write-Through
```java 
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserServiceWriteThrough {

    private final UserRepository userRepository;
    private final RedisTemplate<String, User> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:";

    @Transactional
    public User updateUser(Long id, UserDetailsUpdateDto updateDto) {
        // 1. Обновляем данные в реляционной БД
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(updateDto.getName());
        User savedUser = userRepository.save(user);

        // 2. СИНХРОННО обновляем данные в кэше
        String key = CACHE_KEY_PREFIX + id;
        redisTemplate.opsForValue().set(key, savedUser, Duration.ofMinutes(30));

        return savedUser;
    }
}
```

## Write-Behind (Write-Back)
```java
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceWriteBehind {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "user:";
    private static final String DB_WRITE_QUEUE = "queue:user-updates";

    public void updateUserAsync(User user) {
        // 1. Моментально обновляем кэш в Redis, чтобы пользователь сразу видел изменения
        String cacheKey = CACHE_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user);

        // 2. Кладем ID пользователя в очередь на запись в БД
        // Так как Redis in-memory, это займет доли миллисекунды
        redisTemplate.opsForList().leftPush(DB_WRITE_QUEUE, user);
    }
}
```
### cronjob
```java 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WriteBehindDatabaseWorker {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String DB_WRITE_QUEUE = "queue:user-updates";
    private static final int BATCH_SIZE = 100;

    // Запуск каждые 5 секунд
    @Scheduled(fixedRate = 5000)
    public void flushUpdatesToDatabase() {
        List<User> usersToUpdate = new ArrayList<>();

        // Выгребаем из очереди Redis пачку обновлений (до 100 штук за раз)
        for (int i = 0; i < BATCH_SIZE; i++) {
            User user = (User) redisTemplate.opsForList().rightPop(DB_WRITE_QUEUE);
            if (user == null) {
                break; // Очередь пуста
            }
            usersToUpdate.add(user);
        }

        if (!usersToUpdate.isEmpty()) {
            log.info("Запись пачки из {} обновлений в PostgreSQL...", usersToUpdate.size());
            // Один тяжелый SQL запрос (Bulk Insert/Update) вместо 100 отдельных
            userRepository.saveAll(usersToUpdate); 
        }
    }
}
```