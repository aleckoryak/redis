# Quick Start Guide - Redis Java Samples

## 🚀 Start Here

### 1. Start Docker Containers (Required)
```bash
cd C:\Projects\redis
docker-compose up -d
```

Wait for both containers to be healthy (30-60 seconds):
```bash
docker-compose ps
```

### 1.1 Start Docker Containers using podman (Required)
```bash
cd C:\Projects\redis
podman compose -f docker-compose-redis.yml up -d
```

```bash
cd C:\Projects\redis
podman compose -f docker-compose-postgres.yml up -d
```

Wait for both containers to be healthy (30-60 seconds):
```bash
podman ps
```


### 2. Verify Connectivity

**Redis:**
```bash
podman exec -it redis-sample redis-cli ping
# Expected: PONG
```

**PostgreSQL:**
```bash
podman exec -it  postgres-redis-cache psql -U postgres -d redis_cache_db -h localhost -c "SELECT COUNT(*) FROM users;"
# Expected: 3
```

### 3. Build Modules

```bash
cd C:\Projects\redis
mvn clean install
```

### 4. Run Individual Modules

**Module 1 - String Operations (Jedis):**
```bash
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSample"
```

**Module 1 - String Operations (Lettuce):**
```bash
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSampleLettuce"
```

**Module 2 - Cache Demo (Jedis):**
```bash
mvn -pl redis-app exec:java -Dexec.mainClass="org.example.redis.cache.UserCacheSample"
```

**Module 3 - Session Demo:**
```bash
mvn -pl redis-app exec:java -Dexec.mainClass="org.example.redis.session.UserSessionSample"
```

### 5 Stop Docker Containers using podman (Required)
```bash
cd C:\Projects\redis
podman compose -f docker-compose-redis.yml down
```

```bash
cd C:\Projects\redis
podman compose -f docker-compose-postgres.yml down
```

Wait for both containers to be healthy (30-60 seconds):
```bash
podman ps
```

## 📋 Project Modules

### Module 1: Redis Data Types
- **Files**: `redis-types/src/main/java/org/example/redis/types/`
- **Coverage**: String, List, Set, Hash, Sorted Set
- **Format**: 2 implementations per type (Jedis + Lettuce)

### Module 2: Database Cache
- **Files**: `redis-app/src/main/java/org/example/redis/cache/`
- **Pattern**: Cache-Aside with TTL (1 hour)
- **Database**: PostgreSQL (sample users table)

### Module 3: User Session Management
- **Files**: `redis-app/src/main/java/org/example/redis/session/`
- **Pattern**: Session storage with auto-expiration
- **Features**: Login, validation, keep-alive, logout

## 🔧 Configuration Files

**Redis Config:**
- `redis-types/src/main/resources/redis.properties`
- `redis-app/src/main/resources/redis.properties`
- Default: localhost:6379

**Database Config:**
- `redis-app/src/main/resources/db.properties`
- PostgreSQL: localhost:5432, user: postgres, password: postgres

## 🧪 Running Tests

```bash
mvn test
```

Tests require Redis and PostgreSQL to be running.

## 📚 Documentation

See **README.md** for detailed documentation on all modules, patterns, and best practices.

## 💡 Tips

- All samples include inline comments explaining operations
- Run sample classes directly from each module using `mvn -pl <module> exec:java`
- Each module is independent and can run standalone
- Modify configurations in `*.properties` files as needed
- Check Docker logs: `docker-compose-* logs redis` or `docker-compose-* logs postgres`

## 🐛 Troubleshooting

**Port already in use:**
```bash
docker-compose down
docker-compose up -d

podman compose -f docker-compose-redis.yml up -d
podman compose -f docker-compose-postgres.yml up -d

```

**Connection errors:**
- Ensure both containers are healthy: `docker-compose ps`
- Check logs: `docker-compose logs`

**Build issues:**
```bash
mvn clean install -U
```

---

**Ready to explore Redis? Start with `mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSample"`!**

