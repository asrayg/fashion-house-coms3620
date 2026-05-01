package org.example.ecommerce;

import org.example.model.OnlineOrder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Read-only order listings and lookups.
 */
public class OrderQueryService {

    private final Scanner scanner;
    private final OnlineOrderRepository repository;

    public OrderQueryService(Scanner scanner, OnlineOrderRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void listOrders() {
        List<OnlineOrder> all = repository.loadAll();
        if (all.isEmpty()) {
            System.out.println("No orders on file.");
            return;
        }

        Map<OnlineOrder.Status, List<OnlineOrder>> grouped =
                all.stream().collect(Collectors.groupingBy(OnlineOrder::getStatus));

        System.out.println("\n--- All Orders ---");
        for (OnlineOrder.Status s : OnlineOrder.Status.values()) {
            List<OnlineOrder> group = grouped.getOrDefault(s, Collections.emptyList());
            System.out.println("\n  ▸ " + s + " (" + group.size() + ")");
            if (group.isEmpty()) {
                System.out.println("    (none)");
            } else {
                for (OnlineOrder o : group) {
                    System.out.println("    [" + o.getId() + "] "
                            + o.getCustomerName()
                            + " | Spec: " + o.getSpecId()
                            + " | Qty: " + o.getQuantity()
                            + " | Total: $" + String.format("%.2f", o.getTotalPrice())
                            + " | " + o.getOrderDate());
                }
            }
        }
    }

    public void viewOrderDetails() {
        System.out.print("Order ID: ");
        int id = ECommerceConsole.readInt(scanner);
        OnlineOrder o = repository.findById(id);
        if (o == null) {
            System.out.println("Error: Order not found.");
            return;
        }
        System.out.println("\n" + o);
    }

    public void viewOrdersByCustomer() {
        System.out.print("Customer email: ");
        String email = scanner.nextLine().trim();

        List<OnlineOrder> orders = repository.loadAll().stream()
                .filter(o -> o.getCustomerEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            System.out.println("No orders found for: " + email);
            return;
        }

        double lifetime = orders.stream()
                .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                        && o.getStatus() != OnlineOrder.Status.REFUNDED)
                .mapToDouble(OnlineOrder::getTotalPrice)
                .sum();

        System.out.println("\n--- Orders for " + email + " ---");
        orders.forEach(o -> System.out.println("  " + o));
        System.out.printf("Lifetime value: $%.2f | Total orders: %d%n",
                lifetime, orders.size());
    }
}
