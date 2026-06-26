package org.example.redis.pubsub.domain;

public enum OrderStatus {
    CREATED("Order created"),
    SENT_TO_KITCHEN("Sent to kitchen"),
    BEING_PREPARED("Being prepared"),
    READY_FOR_DELIVERY("Ready for delivery"),
    ASSIGNED_TO_DRIVER("Assigned to driver"),
    IN_DELIVERY("In delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

