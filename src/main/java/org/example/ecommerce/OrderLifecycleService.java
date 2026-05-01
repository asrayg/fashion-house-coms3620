package org.example.ecommerce;

import org.example.model.OnlineOrder;

import java.util.Scanner;

/**
 * Status changes: update, cancel (with inventory semantics), refund.
 */
public class OrderLifecycleService {

    private final Scanner scanner;
    private final OnlineOrderRepository repository;
    private final ECommerceInventoryService inventory;

    public OrderLifecycleService(Scanner scanner,
                                 OnlineOrderRepository repository,
                                 ECommerceInventoryService inventory) {
        this.scanner = scanner;
        this.repository = repository;
        this.inventory = inventory;
    }

    public void updateOrderStatus() {
        System.out.print("Order ID: ");
        int id = ECommerceConsole.readInt(scanner);
        OnlineOrder o = repository.findById(id);
        if (o == null) {
            System.out.println("Error: Order not found.");
            return;
        }
        if (o.getStatus() == OnlineOrder.Status.CANCELLED
                || o.getStatus() == OnlineOrder.Status.REFUNDED) {
            System.out.println("Error: Cannot update a cancelled or refunded order.");
            return;
        }

        System.out.println("Current status: " + o.getStatus());
        System.out.println("1. PENDING");
        System.out.println("2. CONFIRMED");
        System.out.println("3. SHIPPED");
        System.out.println("4. DELIVERED");
        System.out.print("New status: ");

        OnlineOrder.Status newStatus;
        switch (scanner.nextLine().trim()) {
            case "1" -> newStatus = OnlineOrder.Status.PENDING;
            case "2" -> newStatus = OnlineOrder.Status.CONFIRMED;
            case "3" -> newStatus = OnlineOrder.Status.SHIPPED;
            case "4" -> newStatus = OnlineOrder.Status.DELIVERED;
            default  -> {
                System.out.println("Invalid option.");
                return;
            }
        }

        o.setStatus(newStatus);
        repository.update(o);
        System.out.println("Status updated to: " + newStatus);
    }

    public void cancelOrder() {
        System.out.print("Order ID: ");
        int id = ECommerceConsole.readInt(scanner);
        OnlineOrder o = repository.findById(id);
        if (o == null) {
            System.out.println("Error: Order not found.");
            return;
        }
        if (o.getStatus() == OnlineOrder.Status.DELIVERED) {
            System.out.println("Error: Cannot cancel a delivered order. Process a refund instead.");
            return;
        }
        if (o.getStatus() == OnlineOrder.Status.CANCELLED) {
            System.out.println("Order is already cancelled.");
            return;
        }

        System.out.print("Reason for cancellation (optional): ");
        String reason = scanner.nextLine().trim();
        System.out.print("Confirm cancellation? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        inventory.restoreStock(o.getSpecId(), o.getQuantity());

        o.setStatus(OnlineOrder.Status.CANCELLED);
        if (!reason.isEmpty()) o.setNotes("CANCELLED: " + reason);
        repository.update(o);

        System.out.println("Order cancelled. " + o.getQuantity()
                + " units restored to inventory.");
    }

    public void processRefund() {
        System.out.print("Order ID: ");
        int id = ECommerceConsole.readInt(scanner);
        OnlineOrder o = repository.findById(id);
        if (o == null) {
            System.out.println("Error: Order not found.");
            return;
        }
        if (o.getStatus() != OnlineOrder.Status.DELIVERED) {
            System.out.println("Error: Refunds can only be processed for DELIVERED orders.");
            return;
        }

        System.out.print("Refund reason: ");
        String reason = scanner.nextLine().trim();
        if (reason.isEmpty()) {
            System.out.println("Error: Refund reason is required.");
            return;
        }

        System.out.print("Confirm refund of $"
                + String.format("%.2f", o.getTotalPrice()) + "? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Refund aborted.");
            return;
        }

        o.setStatus(OnlineOrder.Status.REFUNDED);
        o.setNotes("REFUNDED: " + reason);
        repository.update(o);

        System.out.printf("Refund of $%.2f processed for order [%d].%n",
                o.getTotalPrice(), o.getId());
    }
}
