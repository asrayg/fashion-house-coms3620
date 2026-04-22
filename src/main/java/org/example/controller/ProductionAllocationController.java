package org.example.controller;

import org.example.model.ProductionAllocation;
import org.example.model.GarmentDesign;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;

public class ProductionAllocationController {
    private static final String FILE = "data/production/production_allocations.csv";
    private Scanner scanner;

    public ProductionAllocationController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void allocateGarmentToProduction() {
        System.out.print("Garment Design ID: ");
        String garmentIdStr = scanner.nextLine().trim();
        int garmentDesignId;
        try {
            garmentDesignId = Integer.parseInt(garmentIdStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid garment design ID.");
            return;
        }

        GarmentDesign garment = GarmentDesignController.findById(garmentDesignId);
        if (garment == null) {
            System.out.println("Garment design not found.");
            return;
        }

        System.out.print("Production Line ID (e.g., LINE-001): ");
        String productionLineId = scanner.nextLine().trim();
        if (productionLineId.isEmpty()) {
            System.out.println("Production line ID cannot be blank.");
            return;
        }

        System.out.print("Target Production Quantity: ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        System.out.print("Deadline (YYYY-MM-DD): ");
        String deadline = scanner.nextLine().trim();
        if (deadline.isEmpty()) {
            System.out.println("Deadline cannot be blank.");
            return;
        }

        int id = FileManager.nextId(FILE);
        String createdDate = LocalDate.now().toString();
        String estimatedCompletionDate = LocalDate.now().plusDays(quantity / 10).toString();
        
        ProductionAllocation allocation = new ProductionAllocation(
            id, garmentDesignId, Integer.parseInt(productionLineId.replaceAll("[^0-9]", "") == "" ? "1" : productionLineId.replaceAll("[^0-9]", "")),
            quantity, deadline, "pending", createdDate, estimatedCompletionDate
        );

        FileManager.appendLine(FILE, allocation.toCSV());
        System.out.println("Production allocation created: " + allocation);
    }

    public void viewProductionAllocations() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No production allocations found.");
            return;
        }
        for (String line : lines) {
            ProductionAllocation allocation = ProductionAllocation.fromCSV(line);
            System.out.println(allocation);
        }
    }

    public static ProductionAllocation findById(int id) {
        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            ProductionAllocation allocation = ProductionAllocation.fromCSV(line);
            if (allocation.getId() == id) {
                return allocation;
            }
        }
        return null;
    }

    public void updateAllocationStatus() {
        System.out.print("Allocation ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid allocation ID.");
            return;
        }

        ProductionAllocation allocation = findById(id);
        if (allocation == null) {
            System.out.println("Allocation not found.");
            return;
        }

        System.out.println("Current status: " + allocation.getStatus());
        System.out.print("New status (pending/in-progress/completed/halted): ");
        String newStatus = scanner.nextLine().trim();
        if (!newStatus.matches("pending|in-progress|completed|halted")) {
            System.out.println("Invalid status.");
            return;
        }

        allocation.setStatus(newStatus);
        List<String> lines = FileManager.readLines(FILE);
        lines.replaceAll(line -> {
            ProductionAllocation p = ProductionAllocation.fromCSV(line);
            return p.getId() == id ? allocation.toCSV() : line;
        });
        FileManager.writeLines(FILE, lines);
        System.out.println("Allocation status updated to: " + newStatus);
    }
}