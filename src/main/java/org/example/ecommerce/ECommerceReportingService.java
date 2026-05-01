package org.example.ecommerce;

import org.example.controller.ProductSpecificationController;
import org.example.model.OnlineOrder;
import org.example.model.ProductSpecification;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Reports, dashboards, stock alerts, and automated status sync.
 */
public class ECommerceReportingService {

    private final Scanner scanner;
    private final OnlineOrderRepository repository;
    private final ECommerceInventoryService inventory;

    public ECommerceReportingService(Scanner scanner,
                                     OnlineOrderRepository repository,
                                     ECommerceInventoryService inventory) {
        this.scanner = scanner;
        this.repository = repository;
        this.inventory = inventory;
    }

    public void salesRevenueReport() {
        List<OnlineOrder> all = repository.loadAll();
        if (all.isEmpty()) {
            System.out.println("No orders on file.");
            return;
        }

        double totalRevenue = all.stream()
                .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                        && o.getStatus() != OnlineOrder.Status.REFUNDED)
                .mapToDouble(OnlineOrder::getTotalPrice)
                .sum();

        double refunded = all.stream()
                .filter(o -> o.getStatus() == OnlineOrder.Status.REFUNDED)
                .mapToDouble(OnlineOrder::getTotalPrice)
                .sum();

        long totalOrders = all.size();
        long activeOrders = all.stream()
                .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                        && o.getStatus() != OnlineOrder.Status.REFUNDED)
                .count();

        double avgOrderValue = activeOrders > 0 ? totalRevenue / activeOrders : 0;

        Map<Integer, Double> bySpec = all.stream()
                .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                        && o.getStatus() != OnlineOrder.Status.REFUNDED)
                .collect(Collectors.groupingBy(OnlineOrder::getSpecId,
                        Collectors.summingDouble(OnlineOrder::getTotalPrice)));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║           SALES REVENUE REPORT               ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf("  Total Revenue     : $%.2f%n", totalRevenue);
        System.out.printf("  Total Refunded    : $%.2f%n", refunded);
        System.out.printf("  Net Revenue       : $%.2f%n", totalRevenue - refunded);
        System.out.printf("  Avg Order Value   : $%.2f%n", avgOrderValue);
        System.out.println("  Total Orders      : " + totalOrders);
        System.out.println("  Active Orders     : " + activeOrders);
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  REVENUE BY PRODUCT SPEC:");
        bySpec.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("    Spec [%d]: $%.2f%n",
                        e.getKey(), e.getValue()));
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    public void lowStockAlert() {
        System.out.print("Low stock threshold (e.g. 10): ");
        int threshold = ECommerceConsole.readInt(scanner);
        if (threshold <= 0) {
            System.out.println("Error: Threshold must be positive.");
            return;
        }

        System.out.println("\n--- Low Stock Alert (threshold: " + threshold + ") ---");
        for (String line : FileManager.readLines(ProductSpecificationController.FILE)) {
            ProductSpecification spec = ProductSpecification.fromCSV(line);
            int stock = inventory.getStockForSpec(spec.getId());
            if (stock <= threshold) {
                System.out.println("  [!] Spec [" + spec.getId() + "] "
                        + spec.getSizeRange()
                        + " | Stock: " + stock
                        + (stock == 0 ? " *** OUT OF STOCK ***" : ""));
            }
        }
    }

    public void orderFulfillmentDashboard() {
        List<OnlineOrder> all = repository.loadAll();
        if (all.isEmpty()) {
            System.out.println("No orders on file.");
            return;
        }

        long pending   = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.PENDING).count();
        long confirmed = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.CONFIRMED).count();
        long shipped   = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.SHIPPED).count();
        long delivered = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.DELIVERED).count();
        long cancelled = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.CANCELLED).count();
        long refunded  = all.stream().filter(o -> o.getStatus() == OnlineOrder.Status.REFUNDED).count();

        double fulfillRate = (all.size() > 0)
                ? (delivered * 100.0 / all.size()) : 0;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║       ORDER FULFILLMENT DASHBOARD            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  PENDING    : " + pending);
        System.out.println("  CONFIRMED  : " + confirmed);
        System.out.println("  SHIPPED    : " + shipped);
        System.out.println("  DELIVERED  : " + delivered);
        System.out.println("  CANCELLED  : " + cancelled);
        System.out.println("  REFUNDED   : " + refunded);
        System.out.println("  ─────────────────────────────────────────");
        System.out.printf("  Fulfillment Rate : %.1f%%%n", fulfillRate);
        System.out.println("  Total Orders     : " + all.size());

        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  NEEDS ATTENTION (PENDING > 3 days):");
        all.stream()
                .filter(o -> o.getStatus() == OnlineOrder.Status.PENDING)
                .filter(o -> {
                    try {
                        return LocalDate.parse(o.getOrderDate())
                                .isBefore(LocalDate.now().minusDays(3));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(o -> System.out.println("    [" + o.getId() + "] "
                        + o.getCustomerName() + " | Ordered: " + o.getOrderDate()));

        System.out.println("╚══════════════════════════════════════════════╝");
    }

    public void syncOrderStatuses() {
        List<OnlineOrder> all = repository.loadAll();
        int updated = 0;

        for (OnlineOrder o : all) {
            if (o.getStatus() == OnlineOrder.Status.CANCELLED
                    || o.getStatus() == OnlineOrder.Status.REFUNDED
                    || o.getStatus() == OnlineOrder.Status.DELIVERED) continue;

            try {
                LocalDate delivery = LocalDate.parse(o.getEstimatedDelivery());
                if (!LocalDate.now().isBefore(delivery)
                        && o.getStatus() == OnlineOrder.Status.SHIPPED) {
                    o.setStatus(OnlineOrder.Status.DELIVERED);
                    repository.update(o);
                    System.out.println("  [" + o.getId() + "] auto-marked DELIVERED.");
                    updated++;
                }
            } catch (Exception ignored) {
                // skip malformed dates
            }
        }

        System.out.println(updated == 0
                ? "All statuses up to date."
                : updated + " order(s) updated.");
    }
}
