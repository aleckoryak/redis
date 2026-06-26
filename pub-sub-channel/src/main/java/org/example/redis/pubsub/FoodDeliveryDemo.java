package org.example.redis.pubsub;

import org.example.redis.pubsub.config.LettuceClientProvider;
import org.example.redis.pubsub.domain.Order;
import org.example.redis.pubsub.load.LoadTestOrchestrator;
import org.example.redis.pubsub.publishers.OrderPublisher;
import org.example.redis.pubsub.subscribers.KitchenSubscriber;
import org.example.redis.pubsub.subscribers.DeliverySubscriber;
import org.example.redis.pubsub.metrics.PubSubMetrics;

/**
 * Food Delivery System - Redis Pub/Sub Demo
 *
 * This application demonstrates Redis Pub/Sub pattern for a food delivery system:
 * 1. Clients place orders through the app
 * 2. Orders are published to the "orders" channel
 * 3. Kitchens subscribe to "orders" channel and prepare food
 * 4. When ready, kitchens publish to "deliveries" channel
 * 5. Drivers subscribe to "deliveries" channel and handle deliveries
 *
 * Run with different configurations:
 *   java -cp target/pub-sub-channel-1.0-SNAPSHOT.jar org.example.redis.pubsub.FoodDeliveryDemo
 *   With JVM args: -Dclients=5 -Dkitchens=2 -Ddrivers=3 -Dorders=3
 */
public class FoodDeliveryDemo {

    public static void main(String[] args) {
        // Parse command line parameters with defaults
        int numClients = Integer.parseInt(System.getProperty("clients", "3"));
        int numKitchens = Integer.parseInt(System.getProperty("kitchens", "2"));
        int numDrivers = Integer.parseInt(System.getProperty("drivers", "3"));
        int ordersPerClient = Integer.parseInt(System.getProperty("orders", "2"));
        long durationSeconds = Long.parseLong(System.getProperty("duration", "60"));

        try {
            // Create orchestrator
            LoadTestOrchestrator orchestrator = new LoadTestOrchestrator(
                    numClients,
                    numKitchens,
                    numDrivers,
                    ordersPerClient
            );

            // Run the load test
            orchestrator.runLoadTest(durationSeconds);

            // Print final metrics
            System.out.println("\n📈 Final Statistics:");
            System.out.println("Total messages: " + orchestrator.getMetrics().getTotalMessages());

        } catch (Exception e) {
            System.err.println("Error running demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            LettuceClientProvider.close();
            System.exit(0);
        }
    }

    /**
     * Simple demo with fixed configuration
     */
    public static void runSimpleDemo() {
        System.out.println("=".repeat(80));
        System.out.println("🚀 SIMPLE FOOD DELIVERY PUB/SUB DEMO");
        System.out.println("=".repeat(80));

        try {
            PubSubMetrics metrics = new PubSubMetrics();

            // Create one kitchen
            KitchenSubscriber kitchen = new KitchenSubscriber("KITCHEN-1", metrics);
            kitchen.startListeningBlocking();

            // Create one driver
            DeliverySubscriber driver = new DeliverySubscriber("DRIVER-1", metrics);
            driver.startListening();

            Thread.sleep(1000); // Let subscribers connect

            // Create order publisher
            OrderPublisher orderPublisher = new OrderPublisher(metrics);

            // Publish sample orders
            System.out.println("\n📦 Publishing sample orders...\n");

            Order order1 = Order.createNew("CUST-1", "Alice", "🍕 Pizza Margherita + 🥤 Coke", "123 Main St");
            orderPublisher.publishOrder(order1);

            Thread.sleep(500);

            Order order2 = Order.createNew("CUST-2", "Bob", "🍔 Burger Combo + 🍟 Fries", "456 Oak Ave");
            orderPublisher.publishOrder(order2);

            Thread.sleep(500);

            Order order3 = Order.createNew("CUST-3", "Charlie", "🍜 Pad Thai + 🥗 Salad", "789 Pine Rd");
            orderPublisher.publishOrder(order3);

            // Wait for processing
            System.out.println("\n⏳ Processing orders (10 seconds)...\n");
            Thread.sleep(10000);

            // Print metrics
            metrics.printReport();

        } catch (Exception e) {
            System.err.println("Error in simple demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            LettuceClientProvider.close();
        }
    }
}


