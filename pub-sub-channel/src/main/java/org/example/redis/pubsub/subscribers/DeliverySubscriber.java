package org.example.redis.pubsub.subscribers;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.example.redis.pubsub.config.RedisConfig;
import org.example.redis.pubsub.domain.DeliveryEvent;
import org.example.redis.pubsub.domain.JsonSerializer;
import org.example.redis.pubsub.metrics.PubSubMetrics;
import org.example.redis.pubsub.publishers.DeliveryPublisher;

import java.util.concurrent.ConcurrentHashMap;

public class DeliverySubscriber {
    private final String driverId;
    private final PubSubMetrics metrics;
    private final DeliveryPublisher deliveryPublisher;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final ConcurrentHashMap<String, DeliveryEvent> activeDeliveries = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public DeliverySubscriber(String driverId, PubSubMetrics metrics) {
        this.driverId = driverId;
        this.metrics = metrics;
        this.deliveryPublisher = new DeliveryPublisher(metrics);
    }

    /**
     * Start listening to deliveries channel
     * Drivers listen for delivery assignments and handle pickups and deliveries
     */
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                // Create dedicated Pub/Sub connection
                RedisURI uri = RedisURI.builder()
                        .withHost(RedisConfig.getHost())
                        .withPort(RedisConfig.getPort())
                        .build();
                RedisClient client = RedisClient.create(uri);
                pubSubConnection = client.connectPubSub();

                System.out.printf("🚗 [DRIVER-%s] Listening for delivery assignments...%n", driverId);

                // Add message listener
                pubSubConnection.addListener(new DeliveryMessageListener());

                // Subscribe to deliveries channel
                pubSubConnection.sync().subscribe("deliveries");

                // Keep connection alive
                while (running) {
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                System.err.println("Error in DeliverySubscriber: " + e.getMessage());
                metrics.recordError();
            }
        }, "Driver-" + driverId);

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Handle delivery assignment
     */
    private void handleDeliveryAssigned(DeliveryEvent event) {
        if (!event.getDriverId().endsWith(driverId)) {
            return; // Not for this driver
        }

        activeDeliveries.put(event.getOrderId(), event);

        System.out.printf("[%tT] 🚗 [DRIVER-%s] Assigned delivery: %s | Address: %s%n",
                System.currentTimeMillis(), driverId, event.getOrderId(), event.getDeliveryAddress());

        // Simulate pickup
        try {
            Thread.sleep(300 + (long) (Math.random() * 700)); // Pickup time: 0.3-1s

            DeliveryEvent pickupEvent = DeliveryEvent.pickedUp(
                    event.getOrderId(),
                    event.getDriverId(),
                    event.getDeliveryAddress()
            );
            deliveryPublisher.publishDeliveryEvent(pickupEvent);

            // Simulate delivery
            Thread.sleep(1000 + (long) (Math.random() * 2000)); // Delivery time: 1-3s

            DeliveryEvent deliveredEvent = DeliveryEvent.delivered(
                    event.getOrderId(),
                    event.getDriverId()
            );
            deliveryPublisher.publishDeliveryEvent(deliveredEvent);

            activeDeliveries.remove(event.getOrderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopListening() {
        running = false;
        try {
            if (pubSubConnection != null && pubSubConnection.isOpen()) {
                pubSubConnection.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing DeliverySubscriber: " + e.getMessage());
        }
    }

    public int getActiveDeliveryCount() {
        return activeDeliveries.size();
    }

    /**
     * Inner class to handle delivery messages from Redis Pub/Sub
     */
    private class DeliveryMessageListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            if ("deliveries".equals(channel)) {
                try {
                    DeliveryEvent event = JsonSerializer.fromJson(message, DeliveryEvent.class);
                    if ("DELIVERY_ASSIGNED".equals(event.getEventType())) {
                        handleDeliveryAssigned(event);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing delivery event: " + e.getMessage());
                    metrics.recordError();
                }
            }
        }
    }
}




