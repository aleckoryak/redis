# Jedis vs Lettuce in `redis-types`

This note explains the practical difference between the two Redis Java clients used in this module.

## Quick Summary

- **Jedis** is simple and direct. It is usually the easiest choice for small-to-medium blocking workloads.
- **Lettuce** is built on Netty and supports synchronous, asynchronous, and reactive styles. It is usually better for high-concurrency or non-blocking designs.

## Side-by-Side Comparison

| Area                       | Jedis                               | Lettuce                                             |
|----------------------------|-------------------------------------|-----------------------------------------------------|
| API style                  | Primarily blocking/synchronous      | Sync + async + reactive                             |
| Typical connection model   | `JedisPool` with borrowed connections | Thread-safe connection and command API              |
| Learning curve             | Lower                               | Moderate                                            |
| High concurrency behavior  | Good with tuned pools               | Usually stronger under very high concurrency        |
| Dependencies/runtime model | Lightweight, straightforward        | Netty-based, more flexible                          |
| Best fit                   | Simple services, scripts, demos     | Scalable services, async pipelines, reactive stacks |
| springboot                 | --                                  | ootb Spring Boot / Spring Data Redis                |
| cluster                    | Поддерживает, но переключение при сбое (failover) может быть медленным.| Отличная динамическая поддержка топологии кластера и быстрое обновление графа узлов.|

## What This Module Demonstrates

For each Redis data type, this module has both implementations:

- `StringSample` and `StringSampleLettuce`
- `ListSample` and `ListSampleLettuce`
- `SetSample` and `SetSampleLettuce`
- `HashSample` and `HashSampleLettuce`
- `SortedSetSample` and `SortedSetSampleLettuce`

This lets you compare equivalent operations with near-identical workflows.

## How to Choose

Use **Jedis** when:

- You want a straightforward blocking client.
- You are comfortable managing pool size and connection borrowing.
- You prioritize simplicity for team onboarding.

Use **Lettuce** when:

- You need async/reactive support or expect high concurrent load.
- You want one client stack that can grow from sync to async/reactive.
- You are building on reactive frameworks.

## Run a Quick Comparison

```bash
mvn -pl redis-types -DskipTests compile
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSample"
mvn -pl redis-types exec:java -Dexec.mainClass="org.example.redis.types.StringSampleLettuce"
```

Compare the output and code style to see operational differences in practice.

