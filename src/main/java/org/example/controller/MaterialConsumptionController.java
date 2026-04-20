package org.example.controller;

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