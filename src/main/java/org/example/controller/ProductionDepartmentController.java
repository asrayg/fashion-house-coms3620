package org.example.controller;

import org.example.model.BatchMaterialRequirement;
import org.example.model.Material;
import org.example.model.MaterialConsumption;
import org.example.model.PerformanceMetric;
import org.example.model.ProductionAllocation;
import org.example.model.ProductionBatch;
import org.example.model.ProductionLine;
import org.example.model.ProductionSchedule;
import org.example.model.QualityCheckpoint;
import org.example.util.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
    private static final String BOM_FILE = "data/materials/batch_material_requirements.csv";
    private static final String MATERIALS_FILE = "data/materials/materials.csv";
    private static final String AUDIT_FILE = "data/production/status_audit_log.csv";

    /** Valid status transitions — key is current status, value is set of allowed next statuses. */
    private static final Map<ProductionBatch.Status, Set<ProductionBatch.Status>> VALID_TRANSITIONS;
    static {
        VALID_TRANSITIONS = new EnumMap<>(ProductionBatch.Status.class);
        VALID_TRANSITIONS.put(ProductionBatch.Status.SCHEDULED,
            EnumSet.of(ProductionBatch.Status.IN_PROGRESS, ProductionBatch.Status.ON_HOLD, ProductionBatch.Status.CANCELLED));
        VALID_TRANSITIONS.put(ProductionBatch.Status.IN_PROGRESS,
            EnumSet.of(ProductionBatch.Status.COMPLETED, ProductionBatch.Status.ON_HOLD, ProductionBatch.Status.CANCELLED));
        VALID_TRANSITIONS.put(ProductionBatch.Status.ON_HOLD,
            EnumSet.of(ProductionBatch.Status.IN_PROGRESS, ProductionBatch.Status.CANCELLED));
        VALID_TRANSITIONS.put(ProductionBatch.Status.COMPLETED, EnumSet.noneOf(ProductionBatch.Status.class));
        VALID_TRANSITIONS.put(ProductionBatch.Status.CANCELLED, EnumSet.noneOf(ProductionBatch.Status.class));
    }

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

    /**
     * UC: Update Production Status
     * Actor: Production Supervisor
     *
     * Features:
     * - Status transition rules (enforces valid state machine)
     * - Audit trail (logs every change with timestamp, user, reason)
     * - Cascading side effects:
     *     COMPLETED  → marks linked allocations as completed
     *     CANCELLED  → marks allocations as halted, returns allocated materials to stock,
     *                   sets BOM entries to RETURNED
     *     ON_HOLD    → marks allocations as halted
     *     IN_PROGRESS → marks allocations as in-progress
     */
    public void updateProductionStatus() {
        List<String> lines = FileManager.readLines(BATCH_FILE);
        if (lines.isEmpty()) {
            System.out.println("No production batches found.");
            return;
        }

        List<ProductionBatch> batches = new ArrayList<>();
        for (String line : lines) {
            batches.add(ProductionBatch.fromCSV(line));
        }

        // ── Display batches grouped by status ──
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║        Update Production Batch Status        ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        for (ProductionBatch.Status status : ProductionBatch.Status.values()) {
            List<ProductionBatch> group = new ArrayList<>();
            for (ProductionBatch b : batches) {
                if (b.getStatus() == status) group.add(b);
            }
            if (!group.isEmpty()) {
                System.out.println("\n  [" + status + "]");
                for (ProductionBatch b : group) {
                    System.out.println("    " + b);
                }
            }
        }

        // ── Select batch ──
        System.out.print("\nEnter Batch ID to update: ");
        int batchId;
        try {
            batchId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Batch ID must be a number.");
            return;
        }

        ProductionBatch target = null;
        for (ProductionBatch batch : batches) {
            if (batch.getId() == batchId) {
                target = batch;
                break;
            }
        }

        if (target == null) {
            System.out.println("Batch not found with ID: " + batchId);
            return;
        }

        ProductionBatch.Status oldStatus = target.getStatus();
        System.out.println("\nSelected: " + target);

        // ── Check if any transitions are allowed ──
        Set<ProductionBatch.Status> allowed = VALID_TRANSITIONS.get(oldStatus);
        if (allowed == null || allowed.isEmpty()) {
            System.out.println("Batch is " + oldStatus + " — no further status changes are allowed.");
            return;
        }

        // ── Show only valid transitions ──
        System.out.println("\nCurrent status: " + oldStatus);
        System.out.println("Valid transitions:");

        List<ProductionBatch.Status> options = new ArrayList<>(allowed);
        for (int i = 0; i < options.size(); i++) {
            String label = formatStatusLabel(options.get(i));
            System.out.println("  " + (i + 1) + ". " + label);
        }
        System.out.print("Select new status (1-" + options.size() + "): ");

        String choice = scanner.nextLine().trim();
        int choiceIndex;
        try {
            choiceIndex = Integer.parseInt(choice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choiceIndex < 0 || choiceIndex >= options.size()) {
            System.out.println("Invalid selection. Please choose 1-" + options.size() + ".");
            return;
        }

        ProductionBatch.Status newStatus = options.get(choiceIndex);

        // ── Collect supervisor info and reason ──
        System.out.print("Supervisor name: ");
        String supervisor = scanner.nextLine().trim();
        if (supervisor.isEmpty()) supervisor = "Unknown";

        System.out.print("Reason for change (optional): ");
        String reason = scanner.nextLine().trim();
        if (reason.isEmpty()) reason = "N/A";

        // ── Confirmation ──
        System.out.println("\n--- Confirm Status Change ---");
        System.out.println("Batch:      " + batchId);
        System.out.println("Transition: " + oldStatus + " -> " + newStatus);
        System.out.println("Supervisor: " + supervisor);
        System.out.println("Reason:     " + reason);
        System.out.print("Proceed? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Status change cancelled.");
            return;
        }

        // ── Apply status change ──
        target.setStatus(newStatus);
        List<String> updatedBatches = new ArrayList<>();
        for (ProductionBatch batch : batches) {
            updatedBatches.add(batch.toCSV());
        }
        FileManager.writeLines(BATCH_FILE, updatedBatches);

        // ── Write audit log entry ──
        // Format: id,batchId,oldStatus,newStatus,supervisor,reason,timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        int auditId = FileManager.nextId(AUDIT_FILE);
        String auditEntry = auditId + "," + batchId + "," + oldStatus + "," + newStatus
            + "," + supervisor + "," + reason.replace(",", ";") + "," + timestamp;
        FileManager.appendLine(AUDIT_FILE, auditEntry);

        System.out.println("\nStatus updated: " + oldStatus + " -> " + newStatus);

        // ── Cascading side effects ──
        int affectedAllocations = cascadeAllocations(target, newStatus);
        int affectedMaterials = 0;

        if (newStatus == ProductionBatch.Status.CANCELLED) {
            affectedMaterials = cascadeMaterialReturn(target);
        }

        // ── Summary ──
        System.out.println("\n--- Update Summary ---");
        System.out.println("Batch " + batchId + " is now " + newStatus + ".");
        System.out.println("Audit log entry #" + auditId + " recorded.");
        if (affectedAllocations > 0) {
            System.out.println("Cascaded to " + affectedAllocations + " production allocation(s).");
        }
        if (affectedMaterials > 0) {
            System.out.println("Returned materials from " + affectedMaterials + " BOM line(s) back to stock.");
        }

        // ── Offer to view audit history ──
        System.out.print("\nView full audit history for this batch? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            viewBatchAuditHistory(batchId);
        }
    }

    /**
     * Cascades the batch status change to linked production allocations.
     * Matches allocations whose productionLineId matches the batch's line
     * and whose deadline matches the batch's estimated completion date.
     */
    private int cascadeAllocations(ProductionBatch batch, ProductionBatch.Status newStatus) {
        List<String> allocationLines = FileManager.readLines(ALLOCATION_FILE);
        if (allocationLines.isEmpty()) return 0;

        String allocationStatus;
        switch (newStatus) {
            case IN_PROGRESS -> allocationStatus = "in-progress";
            case COMPLETED   -> allocationStatus = "completed";
            case ON_HOLD     -> allocationStatus = "halted";
            case CANCELLED   -> allocationStatus = "halted";
            default          -> { return 0; }
        }

        List<ProductionAllocation> allocations = new ArrayList<>();
        for (String line : allocationLines) {
            allocations.add(ProductionAllocation.fromCSV(line));
        }

        int affected = 0;
        for (ProductionAllocation alloc : allocations) {
            if (alloc.getProductionLineId() == batch.getProductionLineId()) {
                String oldAllocStatus = alloc.getStatus();
                if (!oldAllocStatus.equalsIgnoreCase(allocationStatus)) {
                    alloc.setStatus(allocationStatus);
                    affected++;
                }
            }
        }

        if (affected > 0) {
            List<String> updated = new ArrayList<>();
            for (ProductionAllocation alloc : allocations) {
                updated.add(alloc.toCSV());
            }
            FileManager.writeLines(ALLOCATION_FILE, updated);
        }
        return affected;
    }

    /**
     * When a batch is cancelled, returns allocated materials back to stock:
     * 1. Finds all BOM entries for the batch with ALLOCATED status
     * 2. Adds their allocatedQuantity back to the material's stockLevel
     * 3. Sets BOM status to RETURNED
     */
    private int cascadeMaterialReturn(ProductionBatch batch) {
        List<String> bomLines = FileManager.readLines(BOM_FILE);
        if (bomLines.isEmpty()) return 0;

        List<BatchMaterialRequirement> boms = new ArrayList<>();
        for (String line : bomLines) {
            boms.add(BatchMaterialRequirement.fromCSV(line));
        }

        // Find BOM entries for this batch that have allocated materials
        List<BatchMaterialRequirement> toReturn = new ArrayList<>();
        for (BatchMaterialRequirement bom : boms) {
            if (bom.getBatchId() == batch.getId()
                    && bom.getStatus() == BatchMaterialRequirement.Status.ALLOCATED
                    && bom.getAllocatedQuantity() > 0) {
                toReturn.add(bom);
            }
        }

        if (toReturn.isEmpty()) return 0;

        // Load materials for stock update
        List<String> materialLines = FileManager.readLines(MATERIALS_FILE);
        List<Material> materials = new ArrayList<>();
        for (String line : materialLines) {
            materials.add(Material.fromCSV(line));
        }

        int affectedCount = 0;
        for (BatchMaterialRequirement bom : toReturn) {
            int returnQty = bom.getAllocatedQuantity();

            // Find and update the material stock
            for (Material mat : materials) {
                if (mat.getId() == bom.getMaterialId()) {
                    mat.setStockLevel(mat.getStockLevel() + returnQty);
                    System.out.println("  Returned " + returnQty + " units of " + mat.getName()
                        + " to stock (new level: " + mat.getStockLevel() + ")");
                    break;
                }
            }

            bom.setStatus(BatchMaterialRequirement.Status.RETURNED);
            bom.setAllocatedQuantity(0);
            affectedCount++;
        }

        // Save updated BOMs
        List<String> updatedBoms = new ArrayList<>();
        for (BatchMaterialRequirement bom : boms) {
            updatedBoms.add(bom.toCSV());
        }
        FileManager.writeLines(BOM_FILE, updatedBoms);

        // Save updated materials
        List<String> updatedMats = new ArrayList<>();
        for (Material mat : materials) {
            updatedMats.add(mat.toCSV());
        }
        FileManager.writeLines(MATERIALS_FILE, updatedMats);

        return affectedCount;
    }

    /**
     * Displays the full audit trail for a given batch ID.
     */
    private void viewBatchAuditHistory(int batchId) {
        List<String> auditLines = FileManager.readLines(AUDIT_FILE);
        if (auditLines.isEmpty()) {
            System.out.println("No audit history found.");
            return;
        }

        System.out.println("\n--- Audit History for Batch " + batchId + " ---");
        System.out.printf("%-4s %-12s %-14s %-18s %-30s %s%n",
            "ID", "Old Status", "New Status", "Supervisor", "Reason", "Timestamp");
        System.out.println("-".repeat(110));

        boolean found = false;
        for (String line : auditLines) {
            String[] parts = line.split(",", 7);
            if (parts.length >= 7) {
                int logBatchId = Integer.parseInt(parts[1].trim());
                if (logBatchId == batchId) {
                    found = true;
                    System.out.printf("%-4s %-12s %-14s %-18s %-30s %s%n",
                        parts[0].trim(), parts[2].trim(), parts[3].trim(),
                        parts[4].trim(), parts[5].trim(), parts[6].trim());
                }
            }
        }

        if (!found) {
            System.out.println("No entries found for batch " + batchId + ".");
        }
    }

    private String formatStatusLabel(ProductionBatch.Status status) {
        return switch (status) {
            case SCHEDULED   -> "Scheduled";
            case IN_PROGRESS -> "In Progress";
            case COMPLETED   -> "Completed";
            case ON_HOLD     -> "Delayed (On Hold)";
            case CANCELLED   -> "Cancelled";
        };
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
