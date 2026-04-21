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

    static final String FILE = "data/material_orders.csv";

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
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> placeOrder();
                case "2" -> listOrders();
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
