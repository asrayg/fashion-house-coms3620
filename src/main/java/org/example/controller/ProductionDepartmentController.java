package org.example.controller;

import org.example.model.BatchMaterialRequirement;
import org.example.model.MaterialConsumption;
import org.example.model.PerformanceMetric;
import org.example.model.ProductionAllocation;
import org.example.model.ProductionBatch;
import org.example.model.ProductionLine;
import org.example.model.ProductionSchedule;
import org.example.model.QualityCheckpoint;
import org.example.util.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Production Department Management — Iteration 2
 * Actor: Production Manager
 *
 * Consolidates production workflows behind a single menu entry and adds
 * department-level planning, monitoring, and reporting use cases.
 */
public class ProductionDepartmentController {

    private static final String LINE_FILE = "data/production/production_lines.csv";
    private static final String BATCH_FILE = "data/production/production_batches.csv";
    private static final String ALLOCATION_FILE = "data/production/production_allocations.csv";
    private static final String CONSUMPTION_FILE = "data/materials/material_consumption.csv";
    private static final String QUALITY_FILE = "data/production/quality_checkpoints.csv";
    private static final String SCHEDULE_FILE = "data/production/production_schedules.csv";
    private static final String PERFORMANCE_FILE = "data/hr/performance_metrics.csv";

    private final Scanner scanner;
    private final ProductionAllocationController productionAllocationController;
    private final MaterialConsumptionController materialConsumptionController;
    private final QualityCheckpointController qualityCheckpointController;
    private final ProductionScheduleController productionScheduleController;
    private final PerformanceMetricController performanceMetricController;

    public ProductionDepartmentController(Scanner scanner) {
        this.scanner = scanner;
        this.productionAllocationController = new ProductionAllocationController(scanner);
        this.materialConsumptionController = new MaterialConsumptionController(scanner);
        this.qualityCheckpointController = new QualityCheckpointController(scanner);
        this.productionScheduleController = new ProductionScheduleController(scanner);
        this.performanceMetricController = new PerformanceMetricController(scanner);
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         Production Department Management    ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Allocate Garment to Production         ║");
            System.out.println("║  2.  View Production Allocations            ║");
            System.out.println("║  3.  Update Allocation Status               ║");
            System.out.println("║  4.  Track Material Consumption             ║");
            System.out.println("║  5.  View Consumption Records               ║");
            System.out.println("║  6.  View Consumption By Allocation         ║");
            System.out.println("║  7.  Perform Quality Checkpoint             ║");
            System.out.println("║  8.  View Quality Checkpoints               ║");
            System.out.println("║  9.  View Quality By Batch                   ║");
            System.out.println("║ 10.  Create Production Schedule             ║");
            System.out.println("║ 11.  View Production Schedules              ║");
            System.out.println("║ 12.  Update Production Schedule             ║");
            System.out.println("║ 13.  Generate Performance Report            ║");
            System.out.println("║ 14.  View Performance Reports               ║");
            System.out.println("║ 15.  Production Batch Management            ║");
            System.out.println("║ 16.  Production Line Overview               ║");
            System.out.println("║ 17.  Production Operations Dashboard        ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1"  -> productionAllocationController.allocateGarmentToProduction();
                case "2"  -> productionAllocationController.viewProductionAllocations();
                case "3"  -> productionAllocationController.updateAllocationStatus();
                case "4"  -> materialConsumptionController.trackMaterialConsumption();
                case "5"  -> materialConsumptionController.viewConsumptionRecords();
                case "6"  -> materialConsumptionController.viewByAllocation();
                case "7"  -> qualityCheckpointController.performQualityCheckpoint();
                case "8"  -> qualityCheckpointController.viewQualityCheckpoints();
                case "9"  -> qualityCheckpointController.viewByBatch();
                case "10" -> productionScheduleController.createProductionSchedule();
                case "11" -> productionScheduleController.viewSchedules();
                case "12" -> productionScheduleController.updateSchedule();
                case "13" -> performanceMetricController.generatePerformanceReport();
                case "14" -> performanceMetricController.viewPerformanceReports();
                case "15" -> new ProductionBatchController(scanner).menu();
                case "16" -> productionLineOverview();
                case "17" -> productionOperationsDashboard();
                case "0" -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void productionLineOverview() {
        List<String> lines = FileManager.readLines(LINE_FILE);
        if (lines.isEmpty()) {
            System.out.println("No production lines registered.");
            return;
        }

        int active = 0;
        int maintenance = 0;
        int inactive = 0;
        int totalCapacity = 0;

        System.out.println("\n--- Production Line Overview ---");
        for (String line : lines) {
            ProductionLine productionLine = ProductionLine.fromCSV(line);
            totalCapacity += productionLine.getCapacityPerDay();
            switch (productionLine.getStatus()) {
                case ACTIVE -> active++;
                case MAINTENANCE -> maintenance++;
                case INACTIVE -> inactive++;
            }
            System.out.println(productionLine);
        }

        System.out.println("Total lines: " + lines.size());
        System.out.println("Active: " + active + " | Maintenance: " + maintenance + " | Inactive: " + inactive);
        System.out.println("Combined daily capacity: " + totalCapacity + " units");
    }

    private void productionOperationsDashboard() {
        List<String> allocationLines = FileManager.readLines(ALLOCATION_FILE);
        List<String> batchLines = FileManager.readLines(BATCH_FILE);
        List<String> consumptionLines = FileManager.readLines(CONSUMPTION_FILE);
        List<String> qualityLines = FileManager.readLines(QUALITY_FILE);
        List<String> scheduleLines = FileManager.readLines(SCHEDULE_FILE);
        List<String> performanceLines = FileManager.readLines(PERFORMANCE_FILE);

        int allocationCount = allocationLines.size();
        int batchCount = batchLines.size();
        int scheduleCount = scheduleLines.size();
        int reportCount = performanceLines.size();
        int totalAllocatedQuantity = 0;
        int completedAllocations = 0;
        int onHoldBatches = 0;
        int cancelledBatches = 0;
        double totalWaste = 0;
        double totalQuality = 0;

        for (String line : allocationLines) {
            ProductionAllocation allocation = ProductionAllocationController.findById(parseIdSafe(line));
            if (allocation != null) {
                totalAllocatedQuantity += allocation.getQuantity();
                if ("completed".equalsIgnoreCase(allocation.getStatus())) {
                    completedAllocations++;
                }
            }
        }

        for (String line : batchLines) {
            ProductionBatch batch = ProductionBatch.fromCSV(line);
            if (batch.getStatus() == ProductionBatch.Status.ON_HOLD) {
                onHoldBatches++;
            } else if (batch.getStatus() == ProductionBatch.Status.CANCELLED) {
                cancelledBatches++;
            }
        }

        for (String line : consumptionLines) {
            MaterialConsumption consumption = MaterialConsumption.fromCSV(line);
            totalWaste += consumption.getWastePercentage();
        }

        for (String line : qualityLines) {
            QualityCheckpoint checkpoint = QualityCheckpoint.fromCSV(line);
            totalQuality += checkpoint.getPassRate();
        }

        double avgWaste = consumptionLines.isEmpty() ? 0 : totalWaste / consumptionLines.size();
        double avgQuality = qualityLines.isEmpty() ? 0 : totalQuality / qualityLines.size();
        double completionRate = allocationCount == 0 ? 0 : (completedAllocations * 100.0) / allocationCount;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║       PRODUCTION OPERATIONS DASHBOARD        ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  Allocations            : " + allocationCount);
        System.out.println("  Completed Allocations   : " + completedAllocations);
        System.out.println("  Completion Rate         : " + String.format("%.2f", completionRate) + "%");
        System.out.println("  Total Allocated Qty     : " + totalAllocatedQuantity);
        System.out.println("  Production Batches      : " + batchCount);
        System.out.println("  On-Hold Batches         : " + onHoldBatches);
        System.out.println("  Cancelled Batches       : " + cancelledBatches);
        System.out.println("  Schedules               : " + scheduleCount);
        System.out.println("  Quality Checkpoints     : " + qualityLines.size());
        System.out.println("  Avg Quality Pass Rate   : " + String.format("%.2f", avgQuality) + "%");
        System.out.println("  Material Consumption    : " + consumptionLines.size());
        System.out.println("  Avg Waste               : " + String.format("%.2f", avgWaste) + "%");
        System.out.println("  Performance Reports     : " + reportCount);
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private int parseIdSafe(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
