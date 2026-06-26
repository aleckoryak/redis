# Pub/Sub Stream Module - Quick Start Guide

## 🚀 Quick Start (5 Minutes)

### Step 1: Ensure Redis is Running

### Step 2: Build the Module

```bash
mvn clean install 
```

### Step 3: Run Simple Demo

```bash
mvn exec:java "-Dexec.mainClass=org.example.redis.pubsub.FoodDeliveryDemo"
```

You should see output like:
```
🚀 FOOD DELIVERY SYSTEM - REDIS PUB/SUB LOAD TEST
==================================================
🔧 Initializing Subscribers...
👨‍🍳 [KITCHEN-1] Listening for orders...
🚗 [DRIVER-1] Listening for delivery assignments...
✓ 1 kitchen initialized
✓ 1 driver initialized

📦 Publishing sample orders...

[12:07:18] 🛵 [ORDER] Published: ORDER-1719399919645-3872 | Items: 🍕 Pizza Margherita + 🥤 Coke | Delivery: 123 Main St
[12:07:18] 👨‍🍳 [KITCHEN-1] Received order: ORDER-... | Items: 🍕 Pizza Margherita + 🥤 Coke | Prep time: 1485ms
[12:07:20] ✨ [KITCHEN-1] Order ready: ORDER-...
[12:07:20] 🚗 [DELIVERY] Assigned: ORDER-... | Driver: DRIVER-23 | Address: 123 Main St
...
```

---

## 🎯 Load Testing Examples

### Example 1: Light Load (Quick Test - ~30 seconds)

```bash
mvn exec:java '-Dexec.mainClass=org.example.redis.pubsub.FoodDeliveryDemo' '-Dexec.args=-Dclients=5 -Dkitchens=2 -Ddrivers=3 -Dorders=2 -Dduration=60'
```

**Expected Output:**
- 2 clients place 1 order each = 2 orders total
- 1 kitchen processes both orders sequentially
- 1 driver delivers both
- ~30 seconds total


### Example 3: Heavy Load (Stress Test - ~120 seconds)

```bash
mvn exec:java '-Dexec.mainClass=org.example.redis.pubsub.FoodDeliveryDemo' '-Dexec.args=-Dclients=10 -Dkitchens=3 -Ddrivers=5 -Dorders=3 -Dduration=120'
```

**Expected Output:**
- 10 clients × 3 orders = 30 orders
- 3 kitchens working in parallel
- 5 drivers handling deliveries
- Expect some orders to be in flight simultaneously
- Highest throughput and most activity

---

## 📊 Understanding the Output

### Log Format

```
[HH:MM:SS] [EMOJI] [ACTOR] Message details
```

**Examples:**
```
[12:07:18] 🛵 [ORDER] Published: ORDER-xxx | Items: 🍕 Pizza | Delivery: Zone A
[12:07:18] 👨‍🍳 [KITCHEN-1] Received order: ORDER-xxx | Items: 🍕 Pizza | Prep time: 1485ms
[12:07:20] ✨ [KITCHEN-1] Order ready: ORDER-xxx
[12:07:20] 🚗 [DELIVERY] Assigned: ORDER-xxx | Driver: DRIVER-23 | Address: Zone A
[12:07:20] 📦 [DELIVERY] Picked Up: ORDER-xxx | Driver: DRIVER-23
[12:07:22] ✅ [DELIVERY] Completed: ORDER-xxx | Driver: DRIVER-23
```

### Metrics Report (End of Test)

```
📊 LOAD TEST METRICS REPORT
════════════════════════════════════════════════════════════════════════════════
⏱️  Test Duration: 60 seconds
📨 Total Messages Published: 120
📤 Messages/sec Throughput: 2.00 msg/s

📋 Order Lifecycle:
  ✓ Orders Created:    20
  ✓ Orders Received:   20
  ✓ Orders Ready:      19
  ✓ Deliveries Assigned: 19
  ✓ Deliveries Complete: 18

❌ Errors: 1
════════════════════════════════════════════════════════════════════════════════
```

---

## 🔍 Observing Async Communication

### Terminal 1: Run the Load Test

```bash
mvn -pl pub-sub-channel exec:java \
  -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo" \
  -Dexec.args="-Dclients=5 -Dkitchens=2 -Ddrivers=3 -Dorders=2"
```

### Terminal 2: Monitor Redis Messages in Real-Time

```bash
redis-cli
> SUBSCRIBE orders deliveries
```

You'll see published messages in real-time:
```
1) "message"
2) "orders"
3) "{\"order_id\":\"ORDER-...\",\"client_id\":\"CUST-1\",\"items\":\"🍕 Pizza\",...}"
```

### Terminal 3: Check Redis Activity

```bash
redis-cli MONITOR
```

Shows all Redis commands in real-time.

---

## 📋 Configuration Parameters

| Parameter | Default | Example | Description |
|-----------|---------|---------|-------------|
| `-Dclients` | 3 | 10 | Number of concurrent client threads |
| `-Dkitchens` | 2 | 5 | Number of kitchens listening |
| `-Ddrivers` | 3 | 8 | Number of drivers listening |
| `-Dorders` | 2 | 5 | Orders per client |
| `-Dduration` | 60 | 120 | Test duration in seconds |

**Example with Custom Config:**

```bash
mvn -pl pub-sub-channel exec:java \
  -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo" \
  -Dexec.args="-Dclients=15 -Dkitchens=4 -Ddrivers=6 -Dorders=4 -Dduration=180"
```

---

## 🏗️ Module Structure

```
pub-sub-channel/
├── pom.xml                    # Maven config (Lettuce dependency)
├── README.md                  # Detailed documentation
├── PUB_SUB_THEORY.md          # Theoretical guide
├── QUICKSTART.md              # This file
├── src/main/java/org/example/redis/pubsub/
│   ├── FoodDeliveryDemo.java           # Main entry point
│   ├── config/
│   │   ├── LettuceClientProvider.java  # Shared connection
│   │   └── RedisConfig.java            # Configuration
│   ├── domain/
│   │   ├── Order.java
│   │   ├── OrderStatus.java
│   │   ├── KitchenEvent.java
│   │   ├── DeliveryEvent.java
│   │   └── JsonSerializer.java
│   ├── publishers/
│   │   ├── OrderPublisher.java
│   │   └── DeliveryPublisher.java
│   ├── subscribers/
│   │   ├── KitchenSubscriber.java
│   │   └── DeliverySubscriber.java
│   ├── metrics/
│   │   └── PubSubMetrics.java
│   └── load/
│       └── LoadTestOrchestrator.java
└── src/main/resources/
    └── redis.properties
```

---

## 🐛 Troubleshooting

### Redis Connection Error

```
Error: Connection refused
```

**Fix:**
```bash
docker-compose ps  # Check if Redis is running
docker-compose up -d  # Start if not running
redis-cli ping  # Verify connection
```

### Compilation Error

```bash
mvn clean compile -pl pub-sub-channel
```

### Tests Not Running

The module skips tests by default. To run tests if they existed:

```bash
mvn -pl pub-sub-channel test
```

### Port Already in Use

If Redis/PostgreSQL ports are in use:

```bash
# Check what's using port 6379
lsof -i :6379

# Or modify docker-compose to use different ports
```

---

## 📚 Documentation

1. **README.md** - Full module documentation with architecture diagrams
2. **PUB_SUB_THEORY.md** - Deep dive into Pub/Sub, Lettuce internals, pros/cons, production readiness
3. **QUICKSTART.md** - This file with quick examples

---

## ✅ What You'll Learn

After running these examples, you'll understand:

1. **Pub/Sub Pattern**: One publisher, many subscribers, async messaging
2. **Lettuce Async Model**: How modern Redis clients handle concurrency
3. **Async Architecture**: Decoupled, scalable systems without blocking
4. **Load Testing**: Simulating realistic workloads with multiple actors
5. **Real-time Communication**: Event-driven, fire-and-forget messaging
6. **Metrics & Observability**: Tracking async operations

---

## 🎓 Next Steps

1. **Experiment:** Adjust `-Dclients`, `-Dkitchens`, `-Ddrivers` and observe behavior
2. **Monitor:** Open Redis MONITOR in a terminal while tests run
3. **Read Theory:** Check **PUB_SUB_THEORY.md** to understand internals
4. **Extend:** Add order cancellation, kitchen capacity limits, driver routing
5. **Compare:** Test Streams vs. Pub/Sub for your use cases

---

## 🚀 Running Now!

Start with the simple demo:

```bash
mvn -pl pub-sub-channel exec:java -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo"
```

Watch the async communication flow in real-time! 🎯

