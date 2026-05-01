package org.example.ecommerce;

import org.example.model.OnlineOrder;

import java.util.Scanner;

/**
 * E-Commerce & Online Orders — entry point. Delegates to focused services under
 * {@code org.example.ecommerce}.
 *
 * <p>Actor: E-Commerce Manager — Iteration 3</p>
 */
public class ECommerceController {

    /** Kept for callers that referenced the orders CSV path by controller name. */
    public static final String FILE = ECommerceConstants.ORDERS_FILE;

    private final Scanner scanner;
    private final OrderProcessingService processing;
    private final OrderQueryService queries;
    private final OrderLifecycleService lifecycle;
    private final ECommerceReportingService reporting;

    public ECommerceController(Scanner scanner) {
        this.scanner = scanner;
        OnlineOrderRepository repository = new OnlineOrderRepository();
        ECommerceInventoryService inventory = new ECommerceInventoryService(repository);
        this.processing = new OrderProcessingService(scanner, repository, inventory);
        this.queries = new OrderQueryService(scanner, repository);
        this.lifecycle = new OrderLifecycleService(scanner, repository, inventory);
        this.reporting = new ECommerceReportingService(scanner, repository, inventory);
    }

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
                case "1"  -> processing.processOnlineOrder();
                case "2"  -> queries.listOrders();
                case "3"  -> queries.viewOrderDetails();
                case "4"  -> lifecycle.updateOrderStatus();
                case "5"  -> lifecycle.cancelOrder();
                case "6"  -> lifecycle.processRefund();
                case "7"  -> queries.viewOrdersByCustomer();
                case "8"  -> reporting.salesRevenueReport();
                case "9"  -> reporting.lowStockAlert();
                case "10" -> reporting.orderFulfillmentDashboard();
                case "11" -> reporting.syncOrderStatuses();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    /** Same behavior as before refactor; resolves an order by id from CSV. */
    public static OnlineOrder findById(int id) {
        return new OnlineOrderRepository().findById(id);
    }

    /** Core use case — exposed for direct invocation (e.g. tests or submenus). */
    public void processOnlineOrder() {
        processing.processOnlineOrder();
    }
}
