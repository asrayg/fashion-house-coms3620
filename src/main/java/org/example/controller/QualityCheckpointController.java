package org.example.controller;

import org.example.model.QualityCheckpoint;
import org.example.model.ProductionAllocation;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;

public class QualityCheckpointController {
    private static final String FILE = "data/quality_checkpoints.csv";
    private static final double QUALITY_THRESHOLD = 95.0;
    private Scanner scanner;

    public QualityCheckpointController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void performQualityCheckpoint() {
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

        System.out.print("Batch ID (e.g., BATCH-001): ");
        String batchId = scanner.nextLine().trim();
        if (batchId.isEmpty()) {
            System.out.println("Batch ID cannot be blank.");
            return;
        }

        System.out.print("Number of Units Tested: ");
        int unitsTested;
        try {
            unitsTested = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of units.");
            return;
        }
        if (unitsTested <= 0) {
            System.out.println("Units tested must be greater than zero.");
            return;
        }

        System.out.print("Units Passed: ");
        int unitsPassed;
        try {
            unitsPassed = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of units passed.");
            return;
        }
        if (unitsPassed > unitsTested) {
            System.out.println("Units passed cannot exceed units tested.");
            return;
        }

        int unitsFailed = unitsTested - unitsPassed;
        
        int id = FileManager.nextId(FILE);
        String status = "pending";
        String defectNotes = "";
        String severity = "low";

        if (unitsFailed > 0) {
            System.out.print("Defect Description: ");
            defectNotes = scanner.nextLine().trim();

            System.out.print("Severity (low/medium/high/critical): ");
            severity = scanner.nextLine().trim();
            if (!severity.matches("low|medium|high|critical")) {
                System.out.println("Invalid severity level.");
                return;
            }

            QualityCheckpoint tempCheckpoint = new QualityCheckpoint(
                id, allocationId, batchId, unitsTested, unitsPassed, unitsFailed,
                LocalDate.now().toString(), defectNotes, severity, "pending"
            );
            
            if (tempCheckpoint.getPassRate() >= QUALITY_THRESHOLD) {
                status = "approved";
            } else {
                status = "rework";
            }
        } else {
            defectNotes = "No defects";
            status = "approved";
        }

        QualityCheckpoint checkpoint = new QualityCheckpoint(
            id, allocationId, batchId, unitsTested, unitsPassed, unitsFailed,
            LocalDate.now().toString(), defectNotes, severity, status
        );

        FileManager.appendLine(FILE, checkpoint.toCSV());
        System.out.println("Quality checkpoint recorded: " + checkpoint);
        System.out.println("Pass rate: " + String.format("%.2f", checkpoint.getPassRate()) + "%");
        System.out.println("Status: " + status);

        if (status.equals("rework")) {
            System.out.println("ALERT: Batch requires rework. Production supervisor has been notified.");
        } else if (status.equals("approved")) {
            System.out.println("Batch approved for proceeding to next stage.");
        }
    }

    public void viewQualityCheckpoints() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No quality checkpoints found.");
            return;
        }
        for (String line : lines) {
            QualityCheckpoint checkpoint = QualityCheckpoint.fromCSV(line);
            System.out.println(checkpoint);
        }
    }

    public void viewByBatch() {
        System.out.print("Batch ID: ");
        String batchId = scanner.nextLine().trim();

        List<String> lines = FileManager.readLines(FILE);
        boolean found = false;
        for (String line : lines) {
            QualityCheckpoint checkpoint = QualityCheckpoint.fromCSV(line);
            if (checkpoint.getBatchId().equalsIgnoreCase(batchId)) {
                System.out.println(checkpoint);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No quality checkpoints for this batch.");
        }
    }

    public static QualityCheckpoint findById(int id) {
        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            QualityCheckpoint checkpoint = QualityCheckpoint.fromCSV(line);
            if (checkpoint.getId() == id) {
                return checkpoint;
            }
        }
        return null;
    }
}