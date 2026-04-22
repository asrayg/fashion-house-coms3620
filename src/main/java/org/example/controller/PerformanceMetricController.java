package org.example.controller;

import org.example.model.PerformanceMetric;
import org.example.model.ProductionAllocation;
import org.example.model.MaterialConsumption;
import org.example.model.QualityCheckpoint;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;

public class PerformanceMetricController {
    private static final String FILE = "data/hr/performance_metrics.csv";
    private Scanner scanner;

    public PerformanceMetricController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void generatePerformanceReport() {
        System.out.print("Report Start Date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        if (startDate.isEmpty()) {
            System.out.println("Start date cannot be blank.");
            return;
        }

        System.out.print("Report End Date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();
        if (endDate.isEmpty()) {
            System.out.println("End date cannot be blank.");
            return;
        }

        List<String> allocationLines = FileManager.readLines("data/production/production_allocations.csv");
        List<String> consumptionLines = FileManager.readLines("data/materials/material_consumption.csv");
        List<String> qualityLines = FileManager.readLines("data/production/quality_checkpoints.csv");

        int totalDesigns = 0;
        int totalQuantity = 0;
        int completedOnTime = 0;
        int totalCompleted = 0;
        double totalWaste = 0;
        int wasteCount = 0;
        double totalQualityRate = 0;
        int qualityCount = 0;

        for (String line : allocationLines) {
            ProductionAllocation allocation = ProductionAllocation.fromCSV(line);
            if (allocation.getCreatedDate().compareTo(startDate) >= 0 &&
                allocation.getCreatedDate().compareTo(endDate) <= 0) {
                totalDesigns++;
                totalQuantity += allocation.getQuantity();

                if (allocation.getStatus().equals("completed")) {
                    totalCompleted++;
                    if (allocation.getEstimatedCompletionDate().compareTo(allocation.getDeadline()) <= 0) {
                        completedOnTime++;
                    }
                }
            }
        }

        for (String line : consumptionLines) {
            MaterialConsumption consumption = MaterialConsumption.fromCSV(line);
            if (consumption.getConsumptionDate().compareTo(startDate) >= 0 &&
                consumption.getConsumptionDate().compareTo(endDate) <= 0) {
                totalWaste += consumption.getWastePercentage();
                wasteCount++;
            }
        }

        for (String line : qualityLines) {
            QualityCheckpoint checkpoint = QualityCheckpoint.fromCSV(line);
            if (checkpoint.getCheckpointDate().compareTo(startDate) >= 0 &&
                checkpoint.getCheckpointDate().compareTo(endDate) <= 0) {
                totalQualityRate += checkpoint.getPassRate();
                qualityCount++;
            }
        }

        double onTimePercent = totalCompleted > 0 ? (completedOnTime * 100.0) / totalCompleted : 0;
        double avgWaste = wasteCount > 0 ? totalWaste / wasteCount : 0;
        double avgQuality = qualityCount > 0 ? totalQualityRate / qualityCount : 0;
        double lineUtilization = totalDesigns > 0 ? (totalCompleted * 100.0) / totalDesigns : 0;
        double costPerGarment = totalQuantity > 0 ? 50.0 : 0;

        int id = FileManager.nextId(FILE);
        PerformanceMetric metric = new PerformanceMetric(
            id, startDate, endDate, totalDesigns, totalQuantity, onTimePercent, avgWaste,
            avgQuality, lineUtilization, costPerGarment, "N/A", "N/A",
            LocalDate.now().toString()
        );

        FileManager.appendLine(FILE, metric.toCSV());

        System.out.println("\n========== PRODUCTION PERFORMANCE REPORT ==========");
        System.out.println("Period: " + startDate + " to " + endDate);
        System.out.println("Total Designs Produced: " + totalDesigns);
        System.out.println("Total Quantity: " + totalQuantity);
        System.out.println("On-Time Completion: " + String.format("%.2f", onTimePercent) + "%");
        System.out.println("Average Waste: " + String.format("%.2f", avgWaste) + "%");
        System.out.println("Quality Pass Rate: " + String.format("%.2f", avgQuality) + "%");
        System.out.println("Line Utilization: " + String.format("%.2f", lineUtilization) + "%");
        System.out.println("Cost Per Garment: $" + String.format("%.2f", costPerGarment));
        System.out.println("Report Generated: " + LocalDate.now());
        System.out.println("====================================================");
    }

    public void viewPerformanceReports() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No performance reports found.");
            return;
        }
        for (String line : lines) {
            PerformanceMetric metric = PerformanceMetric.fromCSV(line);
            System.out.println(metric);
        }
    }

    public static PerformanceMetric findById(int id) {
        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            PerformanceMetric metric = PerformanceMetric.fromCSV(line);
            if (metric.getId() == id) {
                return metric;
            }
        }
        return null;
    }
}