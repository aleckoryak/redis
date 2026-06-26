package org.example.redis.pubsub.publishers;

import io.lettuce.core.api.sync.RedisCommands;
import org.example.redis.pubsub.config.LettuceClientProvider;
import org.example.redis.pubsub.domain.JsonSerializer;
import org.example.redis.pubsub.domain.Order;
import org.example.redis.pubsub.metrics.PubSubMetrics;

public class OrderPublisher {
    private final RedisCommands<String, String> syncCommands;
    private final PubSubMetrics metrics;
    private static final String CHANNEL_ORDERS = "orders";

    public OrderPublisher(PubSubMetrics metrics) {
        this.syncCommands = LettuceClientProvider.getConnection().sync();
        this.metrics = metrics;
    }

    /**
     * Publish a new order to the "orders" channel
     * This simulates a client placing an order through the food delivery app
     */
    public void publishOrder(Order order) {
        try {
            String orderJson = JsonSerializer.toJson(order);
            long timestamp = System.currentTimeMillis();

            syncCommands.publish(CHANNEL_ORDERS, orderJson);

            metrics.recordOrderCreated(order.getOrderId());

            System.out.printf("[%tT] 🛵 [ORDER] Published: %s | Items: %s | Delivery: %s%n",
                    timestamp, order.getOrderId(), order.getItems(), order.getDeliveryAddress());
        } catch (Exception e) {
            metrics.recordError();
            System.err.println("Error publishing order: " + e.getMessage());
        }
    }

    /**
     * Batch publish multiple orders
     */
    public void publishOrders(Order... orders) {
        for (Order order : orders) {
            publishOrder(order);
            try {
                Thread.sleep(100); // Small delay between orders for realistic simulation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

