package org.example.controller;

import org.example.model.OnlineOrder;
import org.example.model.ProductSpecification;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * E-Commerce & Online Orders Department — Iteration 3
 * Actor: E-Commerce Manager
 *
 * Features:
 *  - Process online orders with inventory validation
 *  - Full order lifecycle management (PENDING → CONFIRMED → SHIPPED → DELIVERED)
 *  - Soft cancel with inventory restoration
 *  - Refund processing
 *  - Customer order history lookup
 *  - Sales revenue reporting
 *  - Low stock alerts
 *  - Order fulfillment dashboard
 *  - Status sync from date logic
 */
public class ECommerceController {

    public static final String FILE = "data/orders.csv";
    private static final int DELIVERY_DAYS = 7;
    private static final double DEFAULT_UNIT_PRICE = 49.99;
    private static final int BASE_STOCK_PER_SPEC = 100;

    private final Scanner scanner;

    public ECommerceController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║      E-Commerce & Online Orders              ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Process Online Order                   ║");
            System.out.println("║  2.  List All Orders                        ║");
            System.out.println("║  3.  View Order Details                     ║");
            System.out.println("║  4.  Update Order Status                    ║");
            System.out.println("║  5.  Cancel Order                           ║");
            System.out.println("║  6.  Process Refund                         ║");
            System.out.println("║  7.  View Orders by Customer                ║");
            System.out.println("║  8.  Sales Revenue Report                   ║");
            System.out.println("║  9.  Low Stock Alert                        ║");
            System.out.println("║ 10.  Order Fulfillment Dashboard            ║");
            System.out.println("║ 11.  Sync Order Statuses                    ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> processOnlineOrder();
                case "2"  -> listOrders();
                case "3"  -> viewOrderDetails();
                case "4"  -> updateOrderStatus();
                case "5"  -> cancelOrder();
                case "6"  -> processRefund();
                case "7"  -> viewOrdersByCustomer();
                case "8"  -> salesRevenueReport();
                case "9"  -> lowStockAlert();
                case "10" -> orderFulfillmentDashboard();
                case "11" -> syncOrderStatuses();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Process Online Order  (Core UC)
    // =========================================================================

    public void processOnlineOrder() {
        System.out.println("\n--- Process Online Order ---");

        System.out.print("Customer name: ");
        String customerName = scanner.nextLine().trim();
        if (customerName.isEmpty()) {
            System.out.println("Error: Customer name is required.");
            return;
        }

        System.out.print("Customer email: ");
        String customerEmail = scanner.nextLine().trim();
        if (!validateEmail(customerEmail)) {
            System.out.println("Error: Invalid email format.");
            return;
        }

        System.out.print("Product Specification ID: ");
        int specId = readInt();
        if (specId == -1) return;

        ProductSpecification spec =
            ProductSpecificationController.findById(specId);
        if (spec == null) {
            System.out.println("Error: Product specification not found.");
            return;
        }

        double unitPrice = DEFAULT_UNIT_PRICE;
        int stock = getStockForSpec(specId);

        System.out.println("Spec: " + spec);
        System.out.printf("Unit price: $%.2f | Stock: %d%n", unitPrice, stock);

        System.out.print("Quantity: ");
        int quantity = readInt();
        if (quantity <= 0) {
            System.out.println("Error: Quantity must be greater than zero.");
            return;
        }
        if (quantity > stock) {
            System.out.println("Error: Insufficient stock. Available: "
                    + stock + ", requested: " + quantity + ".");
            return;
        }

        System.out.print("Shipping address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Payment method (e.g. Credit Card, PayPal): ");
        String payment = scanner.nextLine().trim();

        if (address.isEmpty() || payment.isEmpty()) {
            System.out.println("Error: Shipping address and payment method are required.");
            return;
        }

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        double total = unitPrice * quantity;
        String orderDate = LocalDate.now().toString();
        String estimatedDelivery = LocalDate.now().plusDays(DELIVERY_DAYS).toString();

        deductStock(specId, quantity);

        int id = FileManager.nextId(FILE);
        OnlineOrder order = new OnlineOrder(
            id, customerName, customerEmail,
            specId, quantity, unitPrice, total,
            address, payment,
            orderDate, estimatedDelivery,
            OnlineOrder.Status.PENDING, notes
        );
        FileManager.appendLine(FILE, order.toCSV());

        System.out.println("\nOrder confirmed!");
        System.out.println(order);
        System.out.printf("Remaining stock for spec [%d]: %d%n",
                specId, getStockForSpec(specId));
    }

    // =========================================================================
    // 2. List All Orders
    // =========================================================================

    private void listOrders() {
        List<OnlineOrder> all = loadAll();
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

    // =========================================================================
    // 3. View Order Details
    // =========================================================================

    private void viewOrderDetails() {
        System.out.print("Order ID: ");
        int id = readInt();
        OnlineOrder o = findById(id);
        if (o == null) {
            System.out.println("Error: Order not found.");
            return;
        }
        System.out.println("\n" + o);
    }

    // =========================================================================
    // 4. Update Order Status
    // =========================================================================

    private void updateOrderStatus() {
        System.out.print("Order ID: ");
        int id = readInt();
        OnlineOrder o = findById(id);
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
            default  -> { System.out.println("Invalid option."); return; }
        }

        o.setStatus(newStatus);
        updateOrder(o);
        System.out.println("Status updated to: " + newStatus);
    }

    // =========================================================================
    // 5. Cancel Order
    // =========================================================================

    private void cancelOrder() {
        System.out.print("Order ID: ");
        int id = readInt();
        OnlineOrder o = findById(id);
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

        restoreStock(o.getSpecId(), o.getQuantity());

        o.setStatus(OnlineOrder.Status.CANCELLED);
        if (!reason.isEmpty()) o.setNotes("CANCELLED: " + reason);
        updateOrder(o);

        System.out.println("Order cancelled. " + o.getQuantity()
                + " units restored to inventory.");
    }

    // =========================================================================
    // 6. Process Refund
    // =========================================================================

    private void processRefund() {
        System.out.print("Order ID: ");
        int id = readInt();
        OnlineOrder o = findById(id);
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
        updateOrder(o);

        System.out.printf("Refund of $%.2f processed for order [%d].%n",
                o.getTotalPrice(), o.getId());
    }

    // =========================================================================
    // 7. View Orders by Customer
    // =========================================================================

    private void viewOrdersByCustomer() {
        System.out.print("Customer email: ");
        String email = scanner.nextLine().trim();

        List<OnlineOrder> orders = loadAll().stream()
            .filter(o -> o.getCustomerEmail().equalsIgnoreCase(email))
            .collect(Collectors.toList());

        if (orders.isEmpty()) {
            System.out.println("No orders found for: " + email);
            return;
        }

        double lifetime = orders.stream()
            .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                      && o.getStatus() != OnlineOrder.Status.REFUNDED)
            .mapToDouble(OnlineOrder::getTotalPrice).sum();

        System.out.println("\n--- Orders for " + email + " ---");
        orders.forEach(o -> System.out.println("  " + o));
        System.out.printf("Lifetime value: $%.2f | Total orders: %d%n",
                lifetime, orders.size());
    }

    // =========================================================================
    // 8. Sales Revenue Report
    // =========================================================================

    private void salesRevenueReport() {
        List<OnlineOrder> all = loadAll();
        if (all.isEmpty()) {
            System.out.println("No orders on file.");
            return;
        }

        double totalRevenue = all.stream()
            .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                      && o.getStatus() != OnlineOrder.Status.REFUNDED)
            .mapToDouble(OnlineOrder::getTotalPrice).sum();

        double refunded = all.stream()
            .filter(o -> o.getStatus() == OnlineOrder.Status.REFUNDED)
            .mapToDouble(OnlineOrder::getTotalPrice).sum();

        long totalOrders = all.size();
        long activeOrders = all.stream()
            .filter(o -> o.getStatus() != OnlineOrder.Status.CANCELLED
                      && o.getStatus() != OnlineOrder.Status.REFUNDED).count();

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

    // =========================================================================
    // 9. Low Stock Alert
    // =========================================================================

    private void lowStockAlert() {
        System.out.print("Low stock threshold (e.g. 10): ");
        int threshold = readInt();
        if (threshold <= 0) {
            System.out.println("Error: Threshold must be positive.");
            return;
        }

        System.out.println("\n--- Low Stock Alert (threshold: " + threshold + ") ---");
        for (String line : FileManager.readLines(ProductSpecificationController.FILE)) {
            ProductSpecification spec = ProductSpecification.fromCSV(line);
            int stock = getStockForSpec(spec.getId());
            if (stock <= threshold) {
                System.out.println("  [!] Spec [" + spec.getId() + "] "
                    + spec.getSizeRange()
                    + " | Stock: " + stock
                    + (stock == 0 ? " *** OUT OF STOCK ***" : ""));
            }
        }
    }

    // =========================================================================
    // 10. Order Fulfillment Dashboard
    // =========================================================================

    private void orderFulfillmentDashboard() {
        List<OnlineOrder> all = loadAll();
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
               } catch (Exception e) { return false; }
           })
           .forEach(o -> System.out.println("    [" + o.getId() + "] "
               + o.getCustomerName() + " | Ordered: " + o.getOrderDate()));

        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 11. Sync Order Statuses
    // =========================================================================

    private void syncOrderStatuses() {
        List<OnlineOrder> all = loadAll();
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
                    updateOrder(o);
                    System.out.println("  [" + o.getId() + "] auto-marked DELIVERED.");
                    updated++;
                }
            } catch (Exception ignored) { }
        }

        System.out.println(updated == 0
            ? "All statuses up to date."
            : updated + " order(s) updated.");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private boolean validateEmail(String email) {
        return email.contains("@") && email.contains(".")
                && email.indexOf("@") < email.lastIndexOf(".");
    }

    /**
     * Virtual stock: BASE_STOCK_PER_SPEC minus quantities on non-cancelled, non-refunded orders.
     */
    private int getStockForSpec(int specId) {
        int ordered = loadAll().stream()
            .filter(o -> o.getSpecId() == specId
                      && o.getStatus() != OnlineOrder.Status.CANCELLED
                      && o.getStatus() != OnlineOrder.Status.REFUNDED)
            .mapToInt(OnlineOrder::getQuantity).sum();
        return Math.max(0, BASE_STOCK_PER_SPEC - ordered);
    }

    private void deductStock(int specId, int quantity) {
        // Stock derived from orders in getStockForSpec(); new row appended after this call in process flow.
    }

    private void restoreStock(int specId, int quantity) {
        // Cancellation removes order from active totals via status CANCELLED.
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return -1;
        }
    }

    private List<OnlineOrder> loadAll() {
        List<OnlineOrder> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) {
            list.add(OnlineOrder.fromCSV(line));
        }
        return list;
    }

    private void updateOrder(OnlineOrder updated) {
        List<String> lines = FileManager.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            if (OnlineOrder.fromCSV(lines.get(i)).getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(FILE, lines);
    }

    public static OnlineOrder findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            OnlineOrder o = OnlineOrder.fromCSV(line);
            if (o.getId() == id) return o;
        }
        return null;
    }
}
