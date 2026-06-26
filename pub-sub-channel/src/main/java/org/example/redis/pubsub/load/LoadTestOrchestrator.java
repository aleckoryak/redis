package org.example.redis.pubsub.load;

import org.example.redis.pubsub.domain.Order;
import org.example.redis.pubsub.metrics.PubSubMetrics;
import org.example.redis.pubsub.publishers.OrderPublisher;
import org.example.redis.pubsub.subscribers.DeliverySubscriber;
import org.example.redis.pubsub.subscribers.KitchenSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoadTestOrchestrator coordinates the food delivery simulation with:
 * - Configurable number of clients placing orders
 * - Configurable number of kitchens processing orders
 * - Configurable number of drivers making deliveries
 */
public class LoadTestOrchestrator {
    private final int numClients;
    private final int numKitchens;
    private final int numDrivers;
    private final int ordersPerClient;
    private final PubSubMetrics metrics;

    private final OrderPublisher orderPublisher;
    private final List<KitchenSubscriber> kitchenSubscribers = new ArrayList<>();
    private final List<DeliverySubscriber> driverSubscribers = new ArrayList<>();

    public LoadTestOrchestrator(int numClients, int numKitchens, int numDrivers, int ordersPerClient) {
        this.numClients = numClients;
        this.numKitchens = numKitchens;
        this.numDrivers = numDrivers;
        this.ordersPerClient = ordersPerClient;
        this.metrics = new PubSubMetrics();
        this.orderPublisher = new OrderPublisher(metrics);
    }

    /**
     * Initialize all subscribers (kitchens and drivers)
     */
    public void initializeSubscribers() {
        System.out.println("\n🔧 Initializing Subscribers...");

        // Initialize kitchens
        for (int i = 1; i <= numKitchens; i++) {
            KitchenSubscriber kitchen = new KitchenSubscriber("KITCHEN-" + i, metrics);
            kitchen.startListeningBlocking();
            kitchenSubscribers.add(kitchen);
        }

        // Initialize drivers
        for (int i = 1; i <= numDrivers; i++) {
            DeliverySubscriber driver = new DeliverySubscriber("DRIVER-" + i, metrics);
            driver.startListening();
            driverSubscribers.add(driver);
        }

        System.out.printf("✓ %d kitchens initialized%n", numKitchens);
        System.out.printf("✓ %d drivers initialized%n", numDrivers);
        System.out.println();
    }

    /**
     * Simulate clients placing orders
     */
    public void simulateClientOrders() {
        System.out.println("📋 Starting Client Order Simulation...");
        System.out.printf("Simulating %d clients × %d orders = %d total orders%n",
                numClients, ordersPerClient, numClients * ordersPerClient);
        System.out.println();

        ExecutorService executorService = Executors.newFixedThreadPool(numClients);
        AtomicInteger ordersPlaced = new AtomicInteger(0);

        for (int clientNum = 1; clientNum <= numClients; clientNum++) {
            final int clientId = clientNum;
            executorService.submit(() -> {
                String clientName = "Client-" + clientId;
                String customerId = "CUST-" + clientId;

                for (int orderNum = 1; orderNum <= ordersPerClient; orderNum++) {
                    String[] restaurants = {"🍕 Pizza Palace", "🍔 Burger Queen", "🍜 Noodle House", "🌮 Taco Bell"};
                    String restaurant = restaurants[(int) (Math.random() * restaurants.length)];

                    String[] dishes = {
                            "🍝 Pasta Carbonara", "🍱 Sushi Platter", "🥩 BBQ Ribs",
                            "🥗 Caesar Salad", "🍲 Tom Yum Soup", "🍛 Biryani Rice"
                    };
                    String items = restaurant + " | " + dishes[(int) (Math.random() * dishes.length)];

                    String[] zones = {"Zone A - Downtown", "Zone B - Suburb", "Zone C - Midtown", "Zone D - Harbor"};
                    String address = zones[(int) (Math.random() * zones.length)];

                    Order order = Order.createNew(customerId, clientName, items, address);
                    orderPublisher.publishOrder(order);

                    int placed = ordersPlaced.incrementAndGet();
                    if (placed % 5 == 0) {
                        System.out.printf("  📊 Progress: %d orders placed...%n", placed);
                    }

                    try {
                        Thread.sleep(100 + (long) (Math.random() * 400)); // Random delay between orders
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                System.out.println("⚠️  Order submission timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf("%n✓ All %d orders submitted%n", ordersPlaced.get());
    }

    /**
     * Run the complete load test
     */
    public void runLoadTest(long durationSeconds) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 FOOD DELIVERY SYSTEM - REDIS PUB/SUB LOAD TEST");
        System.out.println("=".repeat(80));

        System.out.printf("Configuration:%n");
        System.out.printf("  📱 Clients: %d%n", numClients);
        System.out.printf("  👨‍🍳 Kitchens: %d%n", numKitchens);
        System.out.printf("  🚗 Drivers: %d%n", numDrivers);
        System.out.printf("  📦 Orders per Client: %d%n", ordersPerClient);
        System.out.printf("  ⏱️  Duration: %d seconds%n", durationSeconds);

        // Initialize subscribers first
        initializeSubscribers();

        // Give subscribers time to connect
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Start client orders
        long startTime = System.currentTimeMillis();
        simulateClientOrders();

        // Wait for processing
        long waitTime = Math.max(1000, durationSeconds * 1000 - (System.currentTimeMillis() - startTime));
        System.out.printf("%n⏳ Waiting %.1f seconds for order processing...%n", waitTime / 1000.0);

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Print metrics
        metrics.printReport();

        // Cleanup
        cleanup();
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        System.out.println("🧹 Cleaning up resources...");

        for (KitchenSubscriber kitchen : kitchenSubscribers) {
            kitchen.stopListening();
        }

        for (DeliverySubscriber driver : driverSubscribers) {
            driver.stopListening();
        }

        System.out.println("✓ Cleanup complete");
    }

    public PubSubMetrics getMetrics() {
        return metrics;
    }
}


