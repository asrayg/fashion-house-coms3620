package org.example.ecommerce;

import org.example.controller.ProductSpecificationController;
import org.example.ecommerce.strategy.Pricing;
import org.example.model.OnlineOrder;
import org.example.model.ProductSpecification;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Core flow: place a new online order with inventory checks.
 */
public class OrderProcessingService {

    private final Scanner scanner;
    private final OnlineOrderRepository repository;
    private final ECommerceInventoryService inventory;

    public OrderProcessingService(Scanner scanner,
                                  OnlineOrderRepository repository,
                                  ECommerceInventoryService inventory) {
        this.scanner = scanner;
        this.repository = repository;
        this.inventory = inventory;
    }

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
        if (!ECommerceConsole.validateEmail(customerEmail)) {
            System.out.println("Error: Invalid email format.");
            return;
        }

        System.out.print("Product Specification ID: ");
        int specId = ECommerceConsole.readInt(scanner);
        if (specId == -1) return;

        ProductSpecification spec =
                ProductSpecificationController.findById(specId);
        if (spec == null) {
            System.out.println("Error: Product specification not found.");
            return;
        }

        double unitPrice = ECommerceConstants.DEFAULT_UNIT_PRICE;
        int stock = inventory.getStockForSpec(specId);

        System.out.println("Spec: " + spec);
        System.out.printf("Unit price: $%.2f | Stock: %d%n", unitPrice, stock);

        System.out.print("Quantity: ");
        int quantity = ECommerceConsole.readInt(scanner);
        if (quantity <= 0) {
            System.out.println("Error: Quantity must be greater than zero.");
            return;
        }
        if (quantity > stock) {
            System.out.println("Error: Insufficient stock. Available: "
                    + stock + ", requested: " + quantity + ".");
            return;
        }

        System.out.println("Pricing type: 1 Standard (full price)  "
                + "2 Bulk (10% off if qty ≥ 10)  3 Loyalty (15% off)");
        System.out.print("Select: ");
        String pricingChoice = scanner.nextLine().trim();
        Pricing.Strategy pricingStrategy = switch (pricingChoice) {
            case "2" -> new Pricing.BulkDiscount();
            case "3" -> new Pricing.Loyalty();
            default -> new Pricing.Standard();
        };
        Pricing.Context pricingContext = new Pricing.Context(pricingStrategy);

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

        double total = pricingContext.calculateTotal(unitPrice, quantity);

        String orderDate = LocalDate.now().toString();
        String estimatedDelivery = LocalDate.now()
                .plusDays(ECommerceConstants.DELIVERY_DAYS).toString();

        inventory.deductStock(specId, quantity);

        int id = FileManager.nextId(ECommerceConstants.ORDERS_FILE);
        OnlineOrder order = new OnlineOrder(
                id, customerName, customerEmail,
                specId, quantity, unitPrice, total,
                address, payment,
                orderDate, estimatedDelivery,
                OnlineOrder.Status.PENDING, notes
        );
        repository.append(order);

        System.out.println("\nOrder confirmed!");
        System.out.println("  " + pricingContext.describe());
        System.out.println(order);
        System.out.printf("Remaining stock for spec [%d]: %d%n",
                specId, inventory.getStockForSpec(specId));
    }
}
