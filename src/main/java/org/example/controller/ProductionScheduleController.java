package org.example.controller;

import org.example.model.ProductionSchedule;
import org.example.model.ProductionAllocation;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductionScheduleController {
    private static final String FILE = "data/production_schedules.csv";
    private Scanner scanner;

    public ProductionScheduleController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void createProductionSchedule() {
        List<String> allocationLines = FileManager.readLines("data/production_allocations.csv");
        if (allocationLines.isEmpty()) {
            System.out.println("No production allocations found.");
            return;
        }

        System.out.println("\nPending Production Allocations:");
        for (String line : allocationLines) {
            ProductionAllocation allocation = ProductionAllocation.fromCSV(line);
            if (allocation.getStatus().equals("pending")) {
                System.out.println("ID: " + allocation.getId() + " | Design: " +
                        allocation.getGarmentDesignId() + " | Qty: " + allocation.getQuantity() +
                        " | Deadline: " + allocation.getDeadline());
            }
        }

        System.out.print("\nAllocation ID to schedule: ");
        int allocationId;
        try {
            allocationId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid allocation ID.");
            return;
        }

        ProductionAllocation allocation = ProductionAllocationController.findById(allocationId);
        if (allocation == null) {
            System.out.println("Allocation not found.");
            return;
        }

        System.out.print("Production Line ID: ");
        String productionLineId = scanner.nextLine().trim();
        if (productionLineId.isEmpty()) {
            System.out.println("Production line ID cannot be blank.");
            return;
        }

        System.out.print("Start Date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        if (startDate.isEmpty()) {
            System.out.println("Start date cannot be blank.");
            return;
        }

        System.out.print("Estimated Duration (hours): ");
        int duration;
        try {
            duration = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration.");
            return;
        }
        if (duration <= 0) {
            System.out.println("Duration must be greater than zero.");
            return;
        }

        System.out.print("Resource Requirements (comma-separated): ");
        String resourceRequirements = scanner.nextLine().trim();

        LocalDate start = LocalDate.parse(startDate);
        LocalDate deadline = LocalDate.parse(allocation.getDeadline());
        long daysAvailable = ChronoUnit.DAYS.between(start, deadline);
        long daysNeeded = (duration + 7) / 8;

        String feasibility = "feasible";
        String bottleneckNotes = "";

        if (daysNeeded > daysAvailable) {
            feasibility = "at-risk";
            bottleneckNotes = "Insufficient time: " + daysNeeded + " days needed, " + daysAvailable + " days available";
        }

        String endDate = start.plusDays(daysNeeded).toString();

        int id = FileManager.nextId(FILE);
        ProductionSchedule schedule = new ProductionSchedule(
            id, allocationId, productionLineId, startDate, endDate, duration,
            resourceRequirements, bottleneckNotes, feasibility, LocalDate.now().toString()
        );

        FileManager.appendLine(FILE, schedule.toCSV());
        System.out.println("Production schedule created: " + schedule);
        System.out.println("Feasibility: " + feasibility);
        if (!bottleneckNotes.isEmpty()) {
            System.out.println("Notes: " + bottleneckNotes);
        }
    }

    public void viewSchedules() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No production schedules found.");
            return;
        }
        for (String line : lines) {
            ProductionSchedule schedule = ProductionSchedule.fromCSV(line);
            System.out.println(schedule);
        }
    }

    public void updateSchedule() {
        System.out.print("Schedule ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid schedule ID.");
            return;
        }

        ProductionSchedule schedule = findById(id);
        if (schedule == null) {
            System.out.println("Schedule not found.");
            return;
        }

        System.out.print("New End Date (YYYY-MM-DD): ");
        String newEndDate = scanner.nextLine().trim();
        if (newEndDate.isEmpty()) {
            System.out.println("End date cannot be blank.");
            return;
        }

        schedule.setEndDate(newEndDate);

        List<String> lines = FileManager.readLines(FILE);
        lines.replaceAll(line -> {
            ProductionSchedule s = ProductionSchedule.fromCSV(line);
            return s.getId() == id ? schedule.toCSV() : line;
        });
        FileManager.writeLines(FILE, lines);
        System.out.println("Schedule updated: " + schedule);
    }

    public static ProductionSchedule findById(int id) {
        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            ProductionSchedule schedule = ProductionSchedule.fromCSV(line);
            if (schedule.getId() == id) {
                return schedule;
            }
        }
        return null;
    }
}