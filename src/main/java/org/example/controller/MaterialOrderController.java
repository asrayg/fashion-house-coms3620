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
        // TODO: Implement UC5 - Place Material Order
        //
        // Steps:
        //   1. Prompt for material ID
        //   2. Use MaterialController.findById(id) → error if null
        //   3. Prompt for: supplierName, quantity, expectedDelivery (e.g. YYYY-MM-DD)
        //   4. Parse quantity as int — reject if <= 0
        //   5. Validate supplierName and expectedDelivery are not blank
        //   6. Generate ID with FileManager.nextId(FILE)
        //   7. Build MaterialOrder with status = PENDING
        //   8. FileManager.appendLine(FILE, order.toCSV())
        //   9. Print confirmation

        System.out.println("[TODO] placeOrder() not yet implemented.");
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
