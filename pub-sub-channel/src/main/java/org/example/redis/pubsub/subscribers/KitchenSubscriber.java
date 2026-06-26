package org.example.redis.pubsub.subscribers;

import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.example.redis.pubsub.config.RedisConfig;
import org.example.redis.pubsub.domain.DeliveryEvent;
import org.example.redis.pubsub.domain.JsonSerializer;
import org.example.redis.pubsub.domain.KitchenEvent;
import org.example.redis.pubsub.domain.Order;
import org.example.redis.pubsub.domain.OrderStatus;
import org.example.redis.pubsub.metrics.PubSubMetrics;
import org.example.redis.pubsub.publishers.DeliveryPublisher;

import java.util.concurrent.ConcurrentHashMap;

public class KitchenSubscriber {
    private final String kitchenId;
    private final PubSubMetrics metrics;
    private final DeliveryPublisher deliveryPublisher;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final ConcurrentHashMap<String, Order> activeOrders = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public KitchenSubscriber(String kitchenId, PubSubMetrics metrics) {
        this.kitchenId = kitchenId;
        this.metrics = metrics;
        this.deliveryPublisher = new DeliveryPublisher(metrics);
    }

    /**
     * Start listening to orders channel
     * When an order arrives, kitchen begins preparation
     */
    public void startListeningBlocking() {
        Thread listenerThread = new Thread(() -> {
            try {
                // Create dedicated Pub/Sub connection
                RedisURI uri = RedisURI.builder()
                        .withHost(RedisConfig.getHost())
                        .withPort(RedisConfig.getPort())
                        .build();
                RedisClient client = RedisClient.create(uri);
                pubSubConnection = client.connectPubSub();

                System.out.printf("👨‍🍳 [KITCHEN-%s] Listening for orders...%n", kitchenId);

                // Add message listener
                pubSubConnection.addListener(new KitchenMessageListener());

                // Subscribe to orders channel
                pubSubConnection.sync().subscribe("orders");

                // Keep connection alive
                while (running) {
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                System.err.println("Error in KitchenSubscriber: " + e.getMessage());
                metrics.recordError();
            }
        }, "Kitchen-Listener-" + kitchenId);

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Handle incoming order
     * Simulate kitchen preparing the order and marking it ready
     */
    private void handleOrderReceived(String orderJson) {
        try {
            Order order = JsonSerializer.fromJson(orderJson, Order.class);
            order.updateStatus(OrderStatus.BEING_PREPARED);

            activeOrders.put(order.getOrderId(), order);
            metrics.recordOrderReceived(order.getOrderId());

            long preparationTime = 500 + (long) (Math.random() * 2000); // 0.5-2.5 seconds simulation

            System.out.printf("[%tT] 👨‍🍳 [KITCHEN-%s] Received order: %s | Items: %s | Prep time: %dms%n",
                    System.currentTimeMillis(), kitchenId, order.getOrderId(), order.getItems(), preparationTime);

            // Simulate cooking time
            Thread.sleep(preparationTime);

            // Mark order as ready
            order.updateStatus(OrderStatus.READY_FOR_DELIVERY);
            KitchenEvent readyEvent = KitchenEvent.ready(order.getOrderId(), kitchenId, System.currentTimeMillis());
            metrics.recordOrderReady(order.getOrderId());

            System.out.printf("[%tT] ✨ [KITCHEN-%s] Order ready: %s%n",
                    System.currentTimeMillis(), kitchenId, order.getOrderId());

            // Publish delivery event
            DeliveryEvent deliveryEvent = DeliveryEvent.assigned(
                    order.getOrderId(),
                    "DRIVER-" + (int) (Math.random() * 100),
                    order.getDeliveryAddress()
            );
            deliveryPublisher.publishDeliveryEvent(deliveryEvent);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error handling order: " + e.getMessage());
            metrics.recordError();
        }
    }

    public void stopListening() {
        running = false;
        try {
            if (pubSubConnection != null && pubSubConnection.isOpen()) {
                pubSubConnection.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing KitchenSubscriber: " + e.getMessage());
        }
    }

    public int getActiveOrderCount() {
        return activeOrders.size();
    }

    /**
     * Inner class to handle order messages from Redis Pub/Sub
     */
    private class KitchenMessageListener extends RedisPubSubAdapter<String, String> {
        @Override
        public void message(String channel, String message) {
            if ("orders".equals(channel)) {
                handleOrderReceived(message);
            }
        }
    }
}



