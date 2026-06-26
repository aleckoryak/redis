# Module 4: Redis Pub/Sub - Food Delivery System

## 📋 Overview

This module demonstrates **Redis Pub/Sub pattern** with a real-world food delivery system scenario where:

1. **Clients** place orders through the mobile app
2. **Orders** are published to a Redis channel (`orders`)
3. **Kitchens** subscribe to the orders channel and prepare food asynchronously
4. **Ready orders** are published to a deliveries channel (`deliveries`)
5. **Drivers** subscribe to the deliveries channel and handle pickups/deliveries
6. **Events flow** through the system asynchronously with real-time visibility

### Key Features

- ✅ **Async Communication**: Orders flow through Redis Pub/Sub without coupling
- ✅ **Multi-Actor Simulation**: Configurable clients, kitchens, and drivers
- ✅ **Load Testing**: Stress test the system with specified concurrency levels
- ✅ **Metrics & Observability**: Real-time logs showing message flow + final statistics
- ✅ **Lettuce Client**: Modern, thread-safe Redis client with reactive capabilities
- ✅ **Production-Ready**: Demonstrates best practices and error handling

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FOOD DELIVERY SYSTEM                      │
└─────────────────────────────────────────────────────────────────┘

┌──────────┐         ┌──────────────┐         ┌──────────┐
│  Clients │         │ Redis        │         │ Kitchens │
│  (Order  │────────▶│ Channel      │────────▶│ (Listen) │
│  Makers) │         │ "orders"     │         │          │
└──────────┘         └──────────────┘         └──────────┘
                            │
                            │ (async, 1-to-many)
                            │
                     ┌──────────────┐
                     │ Process      │
                     │ & Prepare    │
                     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐         ┌──────────┐
                     │ Redis        │         │ Drivers  │
                     │ Channel      │────────▶│ (Listen) │
                     │ "deliveries" │         │          │
                     └──────────────┘         └──────────┘
                            │
                            │ (async, 1-to-many)
                            │
                     ┌──────────────┐
                     │ Deliver      │
                     │ & Complete   │
                     └──────────────┘
```

## 📦 Project Structure

```
pub-sub-channel/
├── pom.xml                              # Maven module config (Lettuce only)
├── README.md                            # This file
├── PUB_SUB_THEORY.md                    # Theoretical documentation
└── src/main/java/org/example/redis/pubsub/
    ├── FoodDeliveryDemo.java            # Main entry point
    ├── config/
    │   ├── LettuceClientProvider.java   # Lettuce connection management
    │   └── RedisConfig.java              # Configuration loading
    ├── domain/
    │   ├── Order.java                    # Order domain model
    │   ├── OrderStatus.java              # Order status enum
    │   ├── KitchenEvent.java             # Kitchen event model
    │   ├── DeliveryEvent.java            # Delivery event model
    │   └── JsonSerializer.java           # JSON utility
    ├── publishers/
    │   ├── OrderPublisher.java           # Publishes to "orders" channel
    │   └── DeliveryPublisher.java        # Publishes to "deliveries" channel
    ├── subscribers/
    │   ├── KitchenSubscriber.java        # Listens to "orders" channel
    │   └── DeliverySubscriber.java       # Listens to "deliveries" channel
    ├── metrics/
    │   └── PubSubMetrics.java            # Event tracking and reporting
    └── load/
        └── LoadTestOrchestrator.java     # Load test orchestrator
```

## 🚀 Quick Start

### Prerequisites

- Docker with Redis running (see parent README)
- Java 26+
- Maven 3.8+

### Step 1: Build

```bash
cd C:\Projects\redis
mvn clean install -pl pub-sub-channel
```

### Step 2: Run Simple Demo

```bash
mvn -pl pub-sub-channel exec:java "-Dexec.mainClass=org.example.redis.pubsub.FoodDeliveryDemo"
```

### Step 3: Run Load Test with Custom Configuration

```bash
# 5 clients, 2 kitchens, 3 drivers, 2 orders each
mvn -pl pub-sub-channel exec:java \
  "-Dexec.mainClass=org.example.redis.pubsub.FoodDeliveryDemo" \
  "-Dexec.args=-Dclients=5 -Dkitchens=2 -Ddrivers=3 -Dorders=2 -Dduration=60"
```

### Step 4: Monitor Redis in Real-Time

In another terminal:

```bash
redis-cli
> SUBSCRIBE orders deliveries
```

Or use Redis CLI monitor:

```bash
podman exec -it redis-sample redis-cli MONITOR
```

## 📊 Load Testing Examples

### Light Load (Quick Test)
```bash
mvn -pl pub-sub-channel exec:java \
  -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo" \
  -Dexec.args="-Dclients=2 -Dkitchens=1 -Ddrivers=1 -Dorders=1 -Dduration=30"
```

**Expected Output:**
```
🚀 FOOD DELIVERY SYSTEM - REDIS PUB/SUB LOAD TEST
==================================================
Configuration:
  📱 Clients: 2
  👨‍🍳 Kitchens: 1
  🚗 Drivers: 1
  📦 Orders per Client: 1
  ⏱️  Duration: 30 seconds

🔧 Initializing Subscribers...
✓ 2 kitchens initialized
✓ 1 driver initialized

📋 Starting Client Order Simulation...
[12:34:56] 🛵 [ORDER] Published: ORDER-xxx | Items: Pizza | Delivery: Zone A
[12:34:56] 👨‍🍳 [KITCHEN-1] Received order: ORDER-xxx | Items: Pizza | Prep time: 1200ms
[12:35:57] ✨ [KITCHEN-1] Order ready: ORDER-xxx
[12:35:57] 🚗 [DRIVER-1] Assigned delivery: ORDER-xxx | Address: Zone A
[12:35:57] 📦 [DELIVERY] Picked Up: ORDER-xxx | Driver: DRIVER-1
[12:35:59] ✅ [DELIVERY] Completed: ORDER-xxx | Driver: DRIVER-1
```

### Medium Load (Normal Test)
```bash
mvn -pl pub-sub-channel exec:java \
  -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo" \
  -Dexec.args="-Dclients=5 -Dkitchens=2 -Ddrivers=3 -Dorders=3 -Dduration=60"
```

### Heavy Load (Stress Test)
```bash
mvn -pl pub-sub-channel exec:java \
  -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo" \
  -Dexec.args="-Dclients=20 -Dkitchens=5 -Ddrivers=10 -Dorders=5 -Dduration=120"
```

## 📈 Metrics & Observability

The test outputs real-time logs showing:

- **Order Events**: When orders are placed, received, ready
- **Kitchen Events**: Processing times, status changes
- **Delivery Events**: Assignments, pickups, completions
- **Latency**: How long orders take from creation to delivery

Final report shows:
```
📊 LOAD TEST METRICS REPORT
════════════════════════════════════════════════════════════════════════════════
⏱️  Test Duration: 60 seconds
📨 Total Messages Published: 245
📤 Messages/sec Throughput: 4.08 msg/s

📋 Order Lifecycle:
  ✓ Orders Created:    50
  ✓ Orders Received:   50
  ✓ Orders Ready:      48
  ✓ Deliveries Assigned: 48
  ✓ Deliveries Complete: 45

❌ Errors: 2
════════════════════════════════════════════════════════════════════════════════
```

## 🏗️ Async Communication Flow

### Order Lifecycle

```
1. CLIENT PLACES ORDER
   └─▶ OrderPublisher.publishOrder(order)
       └─▶ Redis PUBLISH orders channel
           └─▶ [ASYNC] Message in Redis

2. KITCHEN LISTENS (async event)
   └─▶ KitchenSubscriber.handleOrderReceived(order)
       └─▶ Simulate cooking (sleep)
           └─▶ [ASYNC] Process completes

3. KITCHEN PUBLISHES READY EVENT
   └─▶ DeliveryPublisher.publishDeliveryEvent(readyEvent)
       └─▶ Redis PUBLISH deliveries channel
           └─▶ [ASYNC] Message in Redis

4. DRIVER LISTENS (async event)
   └─▶ DeliverySubscriber.handleDeliveryAssigned(event)
       └─▶ Simulate pickup & delivery (sleep)
           └─▶ [ASYNC] Delivery completes

5. DRIVER PUBLISHES COMPLETION
   └─▶ DeliveryPublisher.publishDeliveryEvent(completedEvent)
       └─▶ Redis PUBLISH deliveries channel
           └─▶ Metrics tracked
```

### Key Async Benefits

- ✅ **Decoupled**: Clients don't wait for kitchens; kitchens don't wait for drivers
- ✅ **Scalable**: Add more kitchens/drivers without client code changes
- ✅ **Real-time**: Subscribers react instantly to published events
- ✅ **Reliable**: Events are processed in order within each channel

## 🧪 What to Observe

### Async Communication in Action

1. **Start the test**
2. **Watch the logs**:
   - Multiple orders published rapidly
   - Kitchens process them independently
   - Drivers handle deliveries in parallel
   - No blocking or waiting

### Expected Behavior

- Orders arrive faster than kitchens can process ✓
- Deliveries don't wait for other deliveries ✓
- Multiple kitchens handle orders concurrently ✓
- Multiple drivers deliver in parallel ✓
- No order is lost ✓

### Performance Characteristics

```
Clients: 5  |  Orders: 5 each = 25 orders
Kitchens: 2  |  Delivery Time: ~1-2 sec per order
Drivers: 3   |  Prep Time: ~0.5-2.5 sec per order

Timeline:
0s:   Orders 1-5 published instantly
0-3s: Kitchens process orders concurrently
3s:   Deliveries 1-3 assigned to drivers
3-6s: Drivers handle deliveries concurrently
6s+:  Remaining orders complete

Result: Linear throughput despite sequential-looking code!
```

## 🔌 Configuration Parameters

Edit `redis.properties` or pass command-line parameters:

```bash
# Command-line examples
-Dclients=10        # Number of concurrent clients
-Dkitchens=3        # Number of kitchens
-Ddrivers=5         # Number of drivers
-Dorders=4          # Orders per client
-Dduration=120      # Test duration in seconds
```

## 🧩 Key Classes & Responsibilities

| Class | Purpose |
|-------|---------|
| `FoodDeliveryDemo` | Main entry point, demo orchestrator |
| `LoadTestOrchestrator` | Coordinates multi-threaded simulation |
| `OrderPublisher` | Publishes orders to Redis |
| `DeliveryPublisher` | Publishes delivery events to Redis |
| `KitchenSubscriber` | Listens to orders, simulates cooking |
| `DeliverySubscriber` | Listens to deliveries, simulates delivery |
| `PubSubMetrics` | Tracks events and generates reports |
| `LettuceClientProvider` | Manages Redis connection (thread-safe) |
| `Order` / `DeliveryEvent` | Domain models |

## 💡 Learning Outcomes

After running this module, you'll understand:

1. **Pub/Sub Pattern**: One-to-many async messaging
2. **Lettuce Client**: Modern Redis Pub/Sub API
3. **Async Architecture**: Decoupled, scalable systems
4. **Load Testing**: Simulating concurrent actors
5. **Real-time Systems**: Event-driven design
6. **Metrics & Observability**: Tracking async flows

## 🐛 Troubleshooting

### No messages appearing?

```bash
# Check if Redis is running
docker-compose ps

# Test connection
redis-cli PING

# Check if Lettuce is connecting
# Look for "Subscribed to orders channel" in logs
```

### Compilation errors?

```bash
# Rebuild with clean
mvn clean compile -pl pub-sub-channel

# Check Java version (need 26+)
java -version
```

### Load test too fast or slow?

Adjust parameters:
- Increase `-Dclients` for more concurrent orders
- Reduce `-Dkitchens` to create backlog
- Reduce `-Ddrivers` to slow deliveries

## 📚 Further Reading

See **PUB_SUB_THEORY.md** for:
- Lettuce internals & thread models
- Pub/Sub vs. Streams vs. Queue patterns
- Production readiness checklist
- When to use Pub/Sub vs. alternatives
- Clustering & high availability considerations

## ✅ Checklist: What to Test

- [ ] Run simple demo with 1 kitchen, 1 driver
- [ ] Run with 5 clients, 2 kitchens, 3 drivers
- [ ] Observe real-time logs in console
- [ ] Check Redis MONITOR for published messages
- [ ] Review metrics report for throughput
- [ ] Modify `-Dorders` and see scaling behavior
- [ ] Run stress test with heavy load
- [ ] Compare throughput: light vs. medium vs. heavy load

---

**Ready to run? Start with:** `mvn -pl pub-sub-channel exec:java -Dexec.mainClass="org.example.redis.pubsub.FoodDeliveryDemo"`

