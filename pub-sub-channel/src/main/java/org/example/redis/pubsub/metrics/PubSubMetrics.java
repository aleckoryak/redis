package org.example.redis.pubsub.metrics;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PubSubMetrics {
    private final AtomicLong ordersCreated = new AtomicLong(0);
    private final AtomicLong ordersReceived = new AtomicLong(0);
    private final AtomicLong ordersReady = new AtomicLong(0);
    private final AtomicLong deliveriesAssigned = new AtomicLong(0);
    private final AtomicLong deliveriesComplete = new AtomicLong(0);

    private final AtomicLong totalMessages = new AtomicLong(0);
    private final AtomicLong errors = new AtomicLong(0);

    private final ConcurrentHashMap<String, Long> orderTimestamps = new ConcurrentHashMap<>();
    private final long startTime;

    public PubSubMetrics() {
        this.startTime = System.currentTimeMillis();
    }

    public void recordOrderCreated(String orderId) {
        ordersCreated.incrementAndGet();
        totalMessages.incrementAndGet();
        orderTimestamps.put(orderId + ":created", System.currentTimeMillis());
    }

    public void recordOrderReceived(String orderId) {
        ordersReceived.incrementAndGet();
        totalMessages.incrementAndGet();
        orderTimestamps.put(orderId + ":received", System.currentTimeMillis());
    }

    public void recordOrderReady(String orderId) {
        ordersReady.incrementAndGet();
        totalMessages.incrementAndGet();
        orderTimestamps.put(orderId + ":ready", System.currentTimeMillis());
    }

    public void recordDeliveryAssigned(String orderId) {
        deliveriesAssigned.incrementAndGet();
        totalMessages.incrementAndGet();
        orderTimestamps.put(orderId + ":assigned", System.currentTimeMillis());
    }

    public void recordDeliveryComplete(String orderId) {
        deliveriesComplete.incrementAndGet();
        totalMessages.incrementAndGet();
        orderTimestamps.put(orderId + ":delivered", System.currentTimeMillis());
    }

    public void recordError() {
        errors.incrementAndGet();
    }

    public long getLatency(String orderId, String from, String to) {
        Long fromTime = orderTimestamps.get(orderId + ":" + from);
        Long toTime = orderTimestamps.get(orderId + ":" + to);
        if (fromTime != null && toTime != null) {
            return toTime - fromTime;
        }
        return -1;
    }

    public void printReport() {
        long endTime = System.currentTimeMillis();
        long durationSec = (endTime - startTime) / 1000;
        double throughput = durationSec > 0 ? (double) totalMessages.get() / durationSec : 0;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 LOAD TEST METRICS REPORT");
        System.out.println("=".repeat(80));
        System.out.println(String.format("⏱️  Test Duration: %d seconds", durationSec));
        System.out.println(String.format("📨 Total Messages Published: %d", totalMessages.get()));
        System.out.println(String.format("📤 Messages/sec Throughput: %.2f msg/s", throughput));
        System.out.println();
        System.out.println("📋 Order Lifecycle:");
        System.out.println(String.format("  ✓ Orders Created:    %d", ordersCreated.get()));
        System.out.println(String.format("  ✓ Orders Received:   %d", ordersReceived.get()));
        System.out.println(String.format("  ✓ Orders Ready:      %d", ordersReady.get()));
        System.out.println(String.format("  ✓ Deliveries Assigned: %d", deliveriesAssigned.get()));
        System.out.println(String.format("  ✓ Deliveries Complete: %d", deliveriesComplete.get()));
        System.out.println();
        System.out.println(String.format("❌ Errors: %d", errors.get()));
        System.out.println("=".repeat(80) + "\n");
    }

    public long getTotalMessages() {
        return totalMessages.get();
    }

    public long getOrdersCreated() {
        return ordersCreated.get();
    }

    public long getOrdersReceived() {
        return ordersReceived.get();
    }

    public long getOrdersReady() {
        return ordersReady.get();
    }

    public long getDeliveriesAssigned() {
        return deliveriesAssigned.get();
    }

    public long getDeliveriesComplete() {
        return deliveriesComplete.get();
    }

    public long getErrors() {
        return errors.get();
    }
}

