package org.example.controller;

import org.example.model.Material;
import org.example.model.MaterialOrder;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC5: Place Material Order
 * Actor: Procurement Officer
 *
 * TODO (your name): Implement placeOrder() and listOrders().
 */
public class MaterialOrderController {

    static final String FILE = "data/materials/material_orders.csv";

    private final Scanner scanner;

    public MaterialOrderController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Material Orders ---");
            System.out.println("1. Place Material Order");
            System.out.println("2. List All Orders");
            System.out.println("3. Receive Material Shipment");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> placeOrder();
                case "2" -> listOrders();
                case "3" -> receiveShipment();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Use Case 5 — Place Material Order
    // -------------------------------------------------------------------------

    private void placeOrder() {
        // Precondition: at least one material must be registered
        if (!FileManager.hasRecords(MaterialController.FILE)) {
            System.out.println("Error: No materials registered. Please register a material first (Material Registry).");
            return;
        }

        // Step 1: Show available materials, then prompt for material ID
        System.out.println("\n--- Available Materials ---");
        for (String line : FileManager.readLines(MaterialController.FILE)) {
            System.out.println(Material.fromCSV(line));
        }
        System.out.print("\nEnter Material ID: ");
        int materialId;
        try {
            materialId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Material ID must be a whole number.");
            return;
        }

        // Step 2: Verify material exists
        Material material = MaterialController.findById(materialId);
        if (material == null) {
            System.out.println("Error: No material found with ID " + materialId + ".");
            return;
        }

        // Step 3: Prompt for supplier name
        System.out.print("Enter Supplier Name: ");
        String supplierName = scanner.nextLine().trim();
        if (supplierName.isEmpty()) {
            System.out.println("Error: Supplier name cannot be blank.");
            return;
        }

        // Step 4: Prompt for quantity and reject if <= 0
        System.out.print("Enter Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Quantity must be a whole number.");
            return;
        }
        if (quantity <= 0) {
            System.out.println("Error: Quantity must be greater than 0.");
            return;
        }

        // Step 5: Prompt for expected delivery date
        System.out.print("Enter Expected Delivery Date (YYYY-MM-DD): ");
        String expectedDelivery = scanner.nextLine().trim();
        if (expectedDelivery.isEmpty()) {
            System.out.println("Error: Expected delivery date cannot be blank.");
            return;
        }

        // Step 6: Generate ID
        int id = FileManager.nextId(FILE);

        // Step 7: Build order with status PENDING
        MaterialOrder order = new MaterialOrder(
                id, materialId, supplierName, quantity, expectedDelivery,
                MaterialOrder.Status.PENDING
        );

        // Step 8: Persist to file
        FileManager.appendLine(FILE, order.toCSV());

        // Step 9: Confirm
        System.out.println("Material order placed successfully:");
        System.out.println(order);
    }

    // -------------------------------------------------------------------------
    // Helper — List Orders
    // -------------------------------------------------------------------------

    private void listOrders() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No material orders on file.");
            return;
        }
        System.out.println("\n--- Material Orders ---");
        for (String line : lines) {
            System.out.println(MaterialOrder.fromCSV(line));
        }
    }

    // -------------------------------------------------------------------------
    // Use Case — Receive Material Shipment
    // -------------------------------------------------------------------------

    private void receiveShipment() {
        // Precondition: at least one open order must exist
        List<MaterialOrder> openOrders = FileManager.readLines(FILE).stream()
                .map(MaterialOrder::fromCSV)
                .filter(o -> o.getStatus() == MaterialOrder.Status.PENDING
                          || o.getStatus() == MaterialOrder.Status.PARTIALLY_RECEIVED)
                .toList();

        if (openOrders.isEmpty()) {
            System.out.println("No pending shipments to receive.");
            return;
        }

        System.out.println("\n--- Open Material Orders ---");
        for (MaterialOrder o : openOrders) {
            Material m = MaterialController.findById(o.getMaterialId());
            String matName = (m != null) ? m.getName() : "Unknown";
            System.out.printf("[%d] %s | Supplier: %s | Ordered: %d | Expected: %s | Status: %s%n",
                    o.getId(), matName, o.getSupplierName(), o.getQuantity(),
                    o.getExpectedDelivery(), o.getStatus());
        }

        System.out.print("\nEnter Order ID to receive against: ");
        int orderId;
        try {
            orderId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Order ID must be a whole number.");
            return;
        }

        final int targetId = orderId;
        MaterialOrder order = openOrders.stream()
                .filter(o -> o.getId() == targetId)
                .findFirst().orElse(null);
        if (order == null) {
            System.out.println("Error: No open order found with ID " + orderId + ".");
            return;
        }

        System.out.printf("Order details: %s%n", order);
        System.out.print("Enter quantity received in this shipment: ");
        int received;
        try {
            received = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Quantity must be a whole number.");
            return;
        }

        if (received <= 0) {
            System.out.println("Error: Quantity must be positive.");
            return;
        }
        if (received > order.getQuantity()) {
            System.out.println("Error: Cannot exceed ordered quantity of " + order.getQuantity() + ".");
            return;
        }

        // Update order status
        if (received == order.getQuantity()) {
            order.setStatus(MaterialOrder.Status.RECEIVED);
        } else {
            order.setStatus(MaterialOrder.Status.PARTIALLY_RECEIVED);
        }
        update(order);

        // Update material stock level
        Material material = MaterialController.findById(order.getMaterialId());
        if (material != null) {
            material.setStockLevel(material.getStockLevel() + received);
            MaterialController.update(material);
            System.out.printf("Shipment recorded. %s stock updated to %d units.%n",
                    material.getName(), material.getStockLevel());
        } else {
            System.out.println("Shipment recorded. (Warning: could not update stock — material not found.)");
        }
        System.out.println("Order status: " + order.getStatus());
    }

    /**
     * Used by iteration 2 (Receive Material Shipment) to find a pending order.
     */
    public static MaterialOrder findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            MaterialOrder o = MaterialOrder.fromCSV(line);
            if (o.getId() == id) return o;
        }
        return null;
    }

    /**
     * Persist an updated MaterialOrder (replaces its line in the file).
     */
    public static void update(MaterialOrder updated) {
        List<String> lines = FileManager.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            MaterialOrder o = MaterialOrder.fromCSV(lines.get(i));
            if (o.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(FILE, lines);
    }
}
