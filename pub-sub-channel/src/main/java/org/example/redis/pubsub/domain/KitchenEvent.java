package org.example.redis.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KitchenEvent {
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("kitchen_id")
    private String kitchenId;

    @JsonProperty("items")
    private String items;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("preparation_time_ms")
    private long preparationTimeMs;

    public KitchenEvent() {}

    // Getters
    public String getEventId() { return eventId; }
    public String getOrderId() { return orderId; }
    public String getKitchenId() { return kitchenId; }
    public String getItems() { return items; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
    public long getPreparationTimeMs() { return preparationTimeMs; }

    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setKitchenId(String kitchenId) { this.kitchenId = kitchenId; }
    public void setItems(String items) { this.items = items; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPreparationTimeMs(long preparationTimeMs) { this.preparationTimeMs = preparationTimeMs; }

    public static KitchenEvent fromOrder(Order order, String kitchenId, long preparationTime) {
        KitchenEvent event = new KitchenEvent();
        event.eventId = "KITCHEN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        event.orderId = order.getOrderId();
        event.kitchenId = kitchenId;
        event.items = order.getItems();
        event.eventType = "ORDER_RECEIVED";
        event.timestamp = System.currentTimeMillis();
        event.preparationTimeMs = preparationTime;
        return event;
    }

    public static KitchenEvent ready(String orderId, String kitchenId, long preparedAt) {
        KitchenEvent event = new KitchenEvent();
        event.eventId = "KITCHEN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        event.orderId = orderId;
        event.kitchenId = kitchenId;
        event.eventType = "READY_FOR_DELIVERY";
        event.timestamp = preparedAt;
        return event;
    }
}



