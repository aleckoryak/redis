package org.example.redis.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryEvent {
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("driver_id")
    private String driverId;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("estimated_delivery_time")
    private long estimatedDeliveryTime;

    public DeliveryEvent() {}

    // Getters
    public String getEventId() { return eventId; }
    public String getOrderId() { return orderId; }
    public String getDriverId() { return driverId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
    public long getEstimatedDeliveryTime() { return estimatedDeliveryTime; }

    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setEstimatedDeliveryTime(long estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public static DeliveryEvent assigned(String orderId, String driverId, String deliveryAddress) {
        DeliveryEvent event = new DeliveryEvent();
        event.eventId = "DELIVERY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        event.orderId = orderId;
        event.driverId = driverId;
        event.deliveryAddress = deliveryAddress;
        event.eventType = "DELIVERY_ASSIGNED";
        event.timestamp = System.currentTimeMillis();
        event.estimatedDeliveryTime = System.currentTimeMillis() + (30 * 60 * 1000);
        return event;
    }

    public static DeliveryEvent pickedUp(String orderId, String driverId, String deliveryAddress) {
        DeliveryEvent event = new DeliveryEvent();
        event.eventId = "DELIVERY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        event.orderId = orderId;
        event.driverId = driverId;
        event.deliveryAddress = deliveryAddress;
        event.eventType = "PICKED_UP";
        event.timestamp = System.currentTimeMillis();
        return event;
    }

    public static DeliveryEvent delivered(String orderId, String driverId) {
        DeliveryEvent event = new DeliveryEvent();
        event.eventId = "DELIVERY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        event.orderId = orderId;
        event.driverId = driverId;
        event.eventType = "DELIVERED";
        event.timestamp = System.currentTimeMillis();
        return event;
    }
}



