package org.example.controller;

import org.example.model.Material;
import org.example.model.MaterialConsumption;
import org.example.model.ProductionAllocation;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;

public class MaterialConsumptionController {
    private static final String FILE = "data/material_consumption.csv";
    private Scanner scanner;

    public MaterialConsumptionController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void trackMaterialConsumption() {
        System.out.print("Production Allocation ID: ");
        int allocationId;
        try {
            allocationId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid allocation ID.");
            return;
        }

        ProductionAllocation allocation = ProductionAllocationController.findById(allocationId);
        if (allocation == null) {
            System.out.println("Production allocation not found.");
            return;
        }

        System.out.print("Material ID: ");
        int materialId;
        try {
            materialId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid material ID.");
            return;
        }

        System.out.print("Allocated Quantity: ");
        int allocatedQuantity;
        try {
            allocatedQuantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }
        if (allocatedQuantity <= 0) {
            System.out.println("Allocated quantity must be greater than zero.");
            return;
        }

        System.out.print("Actual Material Used: ");
        int actualQuantity;
        try {
            actualQuantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid actual quantity.");
            return;
        }
        if (actualQuantity > allocatedQuantity) {
            System.out.println("WARNING: Actual consumption exceeds allocation!");
        }

        System.out.print("Waste Quantity: ");
        int wasteQuantity;
        try {
            wasteQuantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid waste quantity.");
            return;
        }

        System.out.print("Units Produced: ");
        int unitsProduced;
        try {
            unitsProduced = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid units produced.");
            return;
        }

        System.out.print("Production Notes: ");
        String notes = scanner.nextLine().trim();

        int id = FileManager.nextId(FILE);
        String consumptionDate = LocalDate.now().toString();
        MaterialConsumption consumption = new MaterialConsumption(
            id, allocationId, materialId, allocatedQuantity, actualQuantity, wasteQuantity, unitsProduced,
            consumptionDate, notes
        );

        FileManager.appendLine(FILE, consumption.toCSV());
        System.out.println("Material consumption tracked: " + consumption);
        System.out.println("Waste percentage: " + String.format("%.2f", consumption.getWastePercentage()) + "%");
        System.out.println("Efficiency rate: " + String.format("%.2f", consumption.getEfficiencyRate()) + "%");
    }

    /**
     * Use Case: Record Material Usage
     * Actor: Production Staff
     * Precondition: A production batch exists and materials are in stock.
     *
     * Main Success Scenario:
     *   1. Staff selects a production batch (ProductionAllocation).
     *   2. Staff enters materials used and quantities consumed.
     *   3. System deducts materials from inventory.
     *   4. System records the usage log.
     *
     * Alternate Flows:
     *   - If inventory is too low, the system rejects the transaction.
     *   - If the batch does not exist, the system refuses the action.
     */
    public void recordMaterialUsage() {
        System.out.print("Production Batch (Allocation) ID: ");
        int allocationId;
        try {
            allocationId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid batch ID.");
            return;
        }

        ProductionAllocation batch = ProductionAllocationController.findById(allocationId);
        if (batch == null) {
            System.out.println("Error: Production batch not found. Action refused.");
            return;
        }
        System.out.println("Selected batch: " + batch);

        System.out.print("Material ID: ");
        int materialId;
        try {
            materialId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid material ID.");
            return;
        }

        Material material = MaterialController.findById(materialId);
        if (material == null) {
            System.out.println("Error: Material not found.");
            return;
        }
        System.out.println("Selected material: " + material);

        System.out.print("Quantity consumed: ");
        int quantityUsed;
        try {
            quantityUsed = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid quantity.");
            return;
        }
        if (quantityUsed <= 0) {
            System.out.println("Error: Quantity must be greater than zero.");
            return;
        }

        if (quantityUsed > material.getStockLevel()) {
            System.out.println("Error: Inventory too low. Available stock: "
                    + material.getStockLevel() + ", requested: " + quantityUsed
                    + ". Transaction rejected.");
            return;
        }

        System.out.print("Units produced from this usage: ");
        int unitsProduced;
        try {
            unitsProduced = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid units produced.");
            return;
        }
        if (unitsProduced < 0) {
            System.out.println("Error: Units produced cannot be negative.");
            return;
        }

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        material.setStockLevel(material.getStockLevel() - quantityUsed);
        MaterialController.update(material);

        int id = FileManager.nextId(FILE);
        MaterialConsumption usage = new MaterialConsumption(
                id, allocationId, materialId,
                quantityUsed,
                quantityUsed,
                0,
                unitsProduced,
                LocalDate.now().toString(),
                notes
        );
        FileManager.appendLine(FILE, usage.toCSV());

        System.out.println("Material usage recorded successfully.");
        System.out.println("  Usage log: " + usage);
        System.out.println("  Remaining stock for '" + material.getName()
                + "': " + material.getStockLevel());
    }

    public void viewConsumptionRecords() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No consumption records found.");
            return;
        }
        for (String line : lines) {
            MaterialConsumption consumption = MaterialConsumption.fromCSV(line);
            System.out.println(consumption);
        }
    }

    public void viewByAllocation() {
        System.out.print("Production Allocation ID: ");
        int allocationId;
        try {
            allocationId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid allocation ID.");
            return;
        }

        List<String> lines = FileManager.readLines(FILE);
        boolean found = false;
        for (String line : lines) {
            MaterialConsumption consumption = MaterialConsumption.fromCSV(line);
            if (consumption.getAllocationId() == allocationId) {
                System.out.println(consumption);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No consumption records for this allocation.");
        }
    }

    public static MaterialConsumption findById(int id) {
        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            MaterialConsumption consumption = MaterialConsumption.fromCSV(line);
            if (consumption.getId() == id) {
                return consumption;
            }
        }
        return null;
    }
}