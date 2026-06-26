package org.example.redis.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("items")
    private String items;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("updated_at")
    private long updatedAt;

    @JsonProperty("estimated_delivery_time")
    private long estimatedDeliveryTime;

    public Order() {}

    public Order(String orderId, String clientId, String clientName, String items, String deliveryAddress,
                 OrderStatus status, long createdAt, long updatedAt, long estimatedDeliveryTime) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getItems() { return items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public long getEstimatedDeliveryTime() { return estimatedDeliveryTime; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setItems(String items) { this.items = items; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setEstimatedDeliveryTime(long estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public static Order createNew(String clientId, String clientName, String items, String deliveryAddress) {
        Order order = new Order();
        order.orderId = "ORDER-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 10000);
        order.clientId = clientId;
        order.clientName = clientName;
        order.items = items;
        order.deliveryAddress = deliveryAddress;
        order.status = OrderStatus.CREATED;
        order.createdAt = System.currentTimeMillis();
        order.updatedAt = System.currentTimeMillis();
        order.estimatedDeliveryTime = System.currentTimeMillis() + (45 * 60 * 1000); // 45 minutes
        return order;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }
}



