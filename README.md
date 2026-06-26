# Redis Java Learning Samples

A comprehensive project demonstrating Redis usage in Java with three practical modules: Redis data types, database caching, and user session management.

## 📋 Project Overview

This project showcases Redis integration with Java using **two popular Redis clients**:
- **Jedis**: Simple, blocking, straightforward API
- **Lettuce**: Modern, async-capable, non-blocking client

### Three Learning Modules

1. **Module 1: Redis Data Types** 
   - String, List, Set, Hash, Sorted Set operations
   - Parallel examples: Jedis vs. Lettuce for each type
   - Use case: Fundamental Redis operations

2. **Module 2: Database Cache (Cache-Aside Pattern)**
   - Cache-aside caching strategy with TTL
   - Database integration (PostgreSQL)
   - Cache invalidation examples
   - Use case: Improve query performance, reduce DB load

3. **Module 3: User Session Management**
   - Session creation, validation, extension
   - Session data storage
   - TTL-based expiration (auto-logout)
   - Use case: Scalable web application sessions

## 🚀 Prerequisites

- **Java 26+** (project configured for Java 26)
- **Maven 3.8+**
- **Docker & Docker Compose** (for Redis + PostgreSQL containers)
- **Git** (optional, for version control)

## 📦 Project Structure

```
redis/
├── pom.xml                     # Maven parent / aggregator POM
├── redis-types/                # Module 1: Redis data type samples
├── redis-app/                  # Modules 2-3: cache + session samples
├── publishhouse/               # Spring Boot publishhouse submodule
├── docker-compose.yml          # Docker services: Redis + PostgreSQL
├── init.sql                    # PostgreSQL initialization script
├── README.md                   # This file
└── .gitignore
```

## 🔧 Setup & Installation

### Step 1: Clone the Repository (optional)

```bash
cd /path/to/projects
git clone <repo-url> redis
cd redis
```

### Step 2: Start Docker Containers

Ensure Docker like container is running, then:

docker
```bash
docker-compose up -d
```
podman
```bash
podman compose -f docker-compose-postgres.yml up -d

```

Verify containers are healthy:
```bash
docker-compose ps
```

Expected output:
```
CONTAINER ID   IMAGE              STATUS
xxx            redis:7-alpine     Up (healthy)
yyy            postgres:15-alpine Up (healthy)
```

### Step 3: Verify Connectivity

Test Redis connection:
```bash
redis-cli ping
```
Expected: `PONG`

Test PostgreSQL connection:
```bash
psql -U postgres -d redis_cache_db -h localhost -c "SELECT COUNT(*) FROM users;"
```
Expected: 3 rows

### Step 4: Build Project

```bash
mvn clean install
```

Dependencies will be downloaded from Maven Central.

## 🎮 Running the Application

### Run Individual Samples

#### Module 1: Data Types
```bash
# Jedis - String Operations
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSample"

# Lettuce - String Operations
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSampleLettuce"

# List, Set, Hash, Sorted Set - similar pattern
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.ListSample"
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.SetSample"
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.HashSample"
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.SortedSetSample"
```

#### Module 2: Database Cache
```bash
# Jedis - Cache with Database
mvn -pl redis-app exec:java -Dexec.mainClass="org.example.redis.cache.UserCacheSample"

# Lettuce - Cache with Database
mvn -pl redis-app exec:java -Dexec.mainClass="org.example.redis.cache.UserCacheLettuceSample"
```

#### Module 3: Session Management
```bash
# Session Demo (Jedis + Lettuce)
mvn -pl redis-app exec:java -Dexec.mainClass="org.example.redis.session.UserSessionSample"
```

### Run Tests

```bash
mvn test
```

To run the new `publishhouse` module specifically:

```bash
mvn -pl publishhouse test
```

## 📊 Module Details

### Module 1: Redis Data Types

Each Redis data type has two implementations side-by-side:

**Strings**: Key-value pairs
- Operations: SET, GET, INCR, APPEND, MSET, MGET
- Use cases: Counters, caching, feature flags

**Lists**: Ordered collections
- Operations: LPUSH, RPUSH, LPOP, RPOP, LRANGE, LTRIM
- Use cases: Queues, activity feeds, logs

**Sets**: Unordered unique collections
- Operations: SADD, SREM, SMEMBERS, SINTER, SUNION, SDIFF
- Use cases: Tagging, unique visitors, memberships

**Hashes**: Field-value pairs (like objects/maps)
- Operations: HSET, HGET, HMGET, HGETALL, HINCRBY
- Use cases: User profiles, objects, configurations

**Sorted Sets**: Ordered by score
- Operations: ZADD, ZRANGE, ZRANK, ZSCORE, ZINCRBY
- Use cases: Leaderboards, rankings, sorted metrics

### Module 2: Database Cache

**Cache-Aside Pattern**:
1. Check cache for key
2. If miss → query database
3. Store result in cache with TTL
4. Return data

**Features**:
- Configurable TTL (default: 1 hour)
- Cache invalidation on updates
- TTL extension on access
- Both Jedis and Lettuce implementations

**DatabaseConnector**: PostgreSQL integration
- `getUserById(id)`: Fetch user by ID
- `getUserByUsername(username)`: Fetch user by username
- `getAllUsers()`: Fetch all users

### Module 3: Session Management

**Session Lifecycle**:
1. **Login**: Create session with 30-minute TTL
2. **Validation**: Check session exists and user ID
3. **Keep-Alive**: Extend TTL on activity
4. **Logout**: Delete session (force expiration)
5. **Auto-Expire**: Redis TTL handles auto-logout

**SessionManager Interface**: Abstract session operations
- `createSession()`: Initialize session
- `getSession()`: Retrieve user ID
- `setSessionData()`: Store custom data
- `extendSession()`: Keep-alive mechanism
- `invalidateSession()`: Logout
- `getAllSessionData()`: Debug/audit

**Implementations**:
- `SessionManagerJedis`: JedisPool-based
- `SessionManagerLettuce`: Lettuce-based

## 🔌 Configuration

### Redis Configuration (`redis-types/src/main/resources/redis.properties` and `redis-app/src/main/resources/redis.properties`)

```properties
redis.host=localhost
redis.port=6379
redis.database=0
redis.password=                    # Empty = no password
redis.timeout=2000
redis.jedis.pool.maxTotal=8        # Max connections in pool
redis.jedis.pool.maxIdle=8         # Max idle connections
redis.jedis.pool.minIdle=2         # Min idle connections
```

### Database Configuration (`redis-app/src/main/resources/db.properties`)

```properties
db.url=jdbc:postgresql://localhost:5432/redis_cache_db
db.user=postgres
db.password=postgres
db.driver=org.postgresql.Driver
```

### Docker Services (`docker-compose-*.yml`)

**Redis** (port 6379):
- Image: `redis:7-alpine`
- Persistence: `--appendonly yes`
- Health check: `redis-cli ping`

**PostgreSQL** (port 5432):
- Image: `postgres:15-alpine`
- Database: `redis_cache_db`
- User: `postgres` / Password: `postgres`
- Init script: `init.sql`

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

Integration tests use real Redis and PostgreSQL instances. Ensure Docker containers are running:

```bash
mvn verify
```

## 📈 Performance Considerations

### Jedis vs. Lettuce

| Aspect | Jedis | Lettuce |
|--------|-------|---------|
| API Style | Blocking | Async-capable |
| Learning Curve | Easy | Moderate |
| Connection Pool | JedisPool | Thread-safe by default |
| Memory Usage | Lower | Higher (async overhead) |
| Best For | Simple apps | High-concurrency apps |

### Connection Pooling

**JedisPool** (Jedis):
- Default: 8 max, 2 min idle connections
- Tunable in `JedisClientProvider.java`
- Use pool for **all** Redis operations (never create new Jedis directly)

**Lettuce**:
- Single shared connection (thread-safe)
- Async operations without thread pool overhead
- Blocking mode (used here) equivalent to Jedis

### Cache TTL Strategy

- **User data**: 1 hour (3600 seconds)
- **Session**: 30 minutes (1800 seconds)
- **Product data**: 2 hours (7200 seconds)
- Adjust based on data freshness requirements

## 🐛 Troubleshooting

### Redis Connection Failed

```
Error: Connection refused
```

**Solution**:
```bash
# Check if Redis is running
docker-compose ps

# Restart containers
docker-compose restart redis

# Verify connectivity
redis-cli ping
```

### PostgreSQL Connection Failed

```
Error: FATAL: password authentication failed for user "postgres"
```

**Solution**:
```bash
# Check credentials in db.properties
# Default: user=postgres, password=postgres

# Restart PostgreSQL
docker-compose restart postgres

# Verify tables exist
docker-compose exec postgres psql -U postgres -d redis_cache_db -c "\dt"
```

### Maven Build Issues

```bash
# Clear cache and rebuild
mvn clean install -U

# Skip tests
mvn clean install -DskipTests
```

### Port Already in Use

```
Error: Ports 6379 or 5432 already in use
```

**Solution**:
```bash
# Check what's using the port
lsof -i :6379
lsof -i :5432

# Option 1: Stop existing service
killall redis-server

# Option 2: Modify docker-compose.yml to use different ports
# Change "6379:6379" to "6380:6379"
```

## 📚 Learning Resources

### Redis Documentation
- [Redis Official Docs](https://redis.io/documentation)
- [Redis Commands Reference](https://redis.io/commands)

### Jedis Library
- [GitHub: Jedis](https://github.com/redis/jedis)
- [Jedis Documentation](https://github.com/redis/jedis/wiki)

### Lettuce Library
- [GitHub: Lettuce](https://github.com/lettuce-io/lettuce-core)
- [Lettuce Documentation](https://lettuce.io/)

### Patterns & Best Practices
- [Redis Patterns](https://redis.io/docs/management/patterns/)
- [Cache-Aside Pattern](https://en.wikipedia.org/wiki/Cache_replacement_policies#Cache-aside)
- [Session Management](https://redis.io/docs/design-patterns/session-management/)

## 💡 Next Steps

1. **Extend the samples**:
   - Add Lua scripting examples
   - Implement Pub/Sub messaging
   - Explore Redis Streams

2. **Production considerations**:
   - Add retry logic & circuit breakers
   - Implement connection pooling tuning
   - Add metrics & monitoring
   - Security: TLS, authentication tokens

3. **Advanced topics**:
   - Redis Cluster
   - Redis Sentinel (HA)
   - Redis Modules
   - Memory optimization

## 📝 License

This project is for educational purposes. Modify and use freely.

## 🤝 Contributing

Feel free to extend and improve:
- Add new modules or examples
- Improve documentation
- Add more test cases
- Share your use cases

---

**Happy Redis learning! 🚀**

