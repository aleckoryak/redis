package org.example.redis.pubsub.publishers;

import io.lettuce.core.api.sync.RedisCommands;
import org.example.redis.pubsub.config.LettuceClientProvider;
import org.example.redis.pubsub.domain.DeliveryEvent;
import org.example.redis.pubsub.domain.JsonSerializer;
import org.example.redis.pubsub.metrics.PubSubMetrics;

public class DeliveryPublisher {
    private final RedisCommands<String, String> syncCommands;
    private final PubSubMetrics metrics;
    private static final String CHANNEL_DELIVERIES = "deliveries";

    public DeliveryPublisher(PubSubMetrics metrics) {
        this.syncCommands = LettuceClientProvider.getConnection().sync();
        this.metrics = metrics;
    }

    /**
     * Publish a delivery event to the "deliveries" channel
     * Kitchen publishes when order is ready, delivery system processes and assigns drivers
     */
    public void publishDeliveryEvent(DeliveryEvent event) {
        try {
            String eventJson = JsonSerializer.toJson(event);
            long timestamp = System.currentTimeMillis();

            syncCommands.publish(CHANNEL_DELIVERIES, eventJson);

            if ("DELIVERY_ASSIGNED".equals(event.getEventType())) {
                metrics.recordDeliveryAssigned(event.getOrderId());
                System.out.printf("[%tT] 🚗 [DELIVERY] Assigned: %s | Driver: %s | Address: %s%n",
                        timestamp, event.getOrderId(), event.getDriverId(), event.getDeliveryAddress());
            } else if ("DELIVERED".equals(event.getEventType())) {
                metrics.recordDeliveryComplete(event.getOrderId());
                System.out.printf("[%tT] ✅ [DELIVERY] Completed: %s | Driver: %s%n",
                        timestamp, event.getOrderId(), event.getDriverId());
            } else if ("PICKED_UP".equals(event.getEventType())) {
                System.out.printf("[%tT] 📦 [DELIVERY] Picked Up: %s | Driver: %s%n",
                        timestamp, event.getOrderId(), event.getDriverId());
            }
        } catch (Exception e) {
            metrics.recordError();
            System.err.println("Error publishing delivery event: " + e.getMessage());
        }
    }
}

