package org.example.controller;

import org.example.model.*;
import org.example.util.FileManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UC6 (Iteration 2): Schedule Production Batch
 * Actor: Production Manager
 * Code Owner: Asray Gopa
 *
 * Features:
 *  - Schedule batch with BOM material checking, line capacity, date validation,
 *    cost estimation, priority, and confirmation workflow
 *  - List / filter batches by status, priority, date range
 *  - View detailed batch breakdown (materials, spec, line info)
 *  - Cancel batch with automatic material restoration
 *  - Put batch on hold / resume
 *  - Register and manage production lines
 *  - Production calendar view
 *  - Batch statistics dashboard
 */
public class ProductionBatchController {

    static final String BATCH_FILE = "data/production/production_batches.csv";
    static final String BOM_FILE   = "data/materials/batch_material_requirements.csv";
    static final String LINE_FILE  = "data/production/production_lines.csv";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_BATCH_QUANTITY = 10000;
    private static final int MIN_BATCH_QUANTITY = 1;
    private static final double MATERIAL_UNITS_PER_GARMENT = 3;
    private static final double LABOR_COST_PER_UNIT = 12.50;
    private static final double OVERHEAD_MULTIPLIER = 1.15;

    private final Scanner scanner;

    public ProductionBatchController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║     Production Batch Management         ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Schedule Production Batch           ║");
            System.out.println("║  2. List All Batches                    ║");
            System.out.println("║  3. Filter Batches                      ║");
            System.out.println("║  4. View Batch Details                  ║");
            System.out.println("║  5. Cancel Batch                        ║");
            System.out.println("║  6. Hold / Resume Batch                 ║");
            System.out.println("║  7. Register Production Line            ║");
            System.out.println("║  8. List Production Lines               ║");
            System.out.println("║  9. Production Calendar                 ║");
            System.out.println("║ 10. Batch Statistics Dashboard          ║");
            System.out.println("║  0. Back                                ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> scheduleBatch();
                case "2"  -> listAllBatches();
                case "3"  -> filterBatches();
                case "4"  -> viewBatchDetails();
                case "5"  -> cancelBatch();
                case "6"  -> holdResumeBatch();
                case "7"  -> registerProductionLine();
                case "8"  -> listProductionLines();
                case "9"  -> productionCalendar();
                case "10" -> batchStatisticsDashboard();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Schedule Production Batch  (main use case — very complex)
    // =========================================================================

    private void scheduleBatch() {
        System.out.println("\n--- Schedule Production Batch ---");

        // ---- Step 1: Check preconditions ----
        if (!FileManager.hasRecords(ProductSpecificationController.FILE)) {
            System.out.println("Error: No product specifications exist. Please define a specification first.");
            return;
        }
        if (!FileManager.hasRecords(MaterialController.FILE)) {
            System.out.println("Error: No materials registered. Cannot check material availability.");
            return;
        }
        if (!FileManager.hasRecords(LINE_FILE)) {
            System.out.println("Error: No production lines registered. Please register a production line first.");
            return;
        }

        // ---- Step 2: Display available specifications with linked garment + collection info ----
        System.out.println("\n--- Available Product Specifications ---");
        List<String> specLines = FileManager.readLines(ProductSpecificationController.FILE);
        for (String line : specLines) {
            ProductSpecification spec = ProductSpecification.fromCSV(line);
            GarmentDesign garment = GarmentDesignController.findById(spec.getGarmentDesignId());
            String garmentInfo = (garment != null)
                ? garment.getName() + " (" + garment.getType() + ", " + garment.getStyle() + ")"
                : "Unknown Garment";
            String collectionInfo = "";
            if (garment != null) {
                org.example.model.Collection col = CollectionController.findById(garment.getCollectionId());
                collectionInfo = (col != null) ? " | Collection: " + col.getName() : "";
            }
            System.out.println("  Spec " + spec + collectionInfo + " | Garment: " + garmentInfo);
        }

        // ---- Step 3: Select product specification ----
        System.out.print("\nEnter Product Specification ID: ");
        int specId;
        try {
            specId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Specification ID must be a number.");
            return;
        }
        ProductSpecification spec = ProductSpecificationController.findById(specId);
        if (spec == null) {
            System.out.println("Error: Product specification with ID " + specId + " does not exist. Batch not scheduled.");
            return;
        }

        // ---- Step 4: Enter batch quantity with min/max validation ----
        System.out.print("Enter batch quantity (" + MIN_BATCH_QUANTITY + "-" + MAX_BATCH_QUANTITY + "): ");
        int batchQty;
        try {
            batchQty = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Batch quantity must be a whole number.");
            return;
        }
        if (batchQty < MIN_BATCH_QUANTITY || batchQty > MAX_BATCH_QUANTITY) {
            System.out.println("Error: Batch quantity must be between " + MIN_BATCH_QUANTITY
                             + " and " + MAX_BATCH_QUANTITY + ".");
            return;
        }

        // ---- Step 5: Enter production date with full validation ----
        System.out.print("Enter production start date (YYYY-MM-DD): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate productionDate;
        try {
            productionDate = LocalDate.parse(dateStr, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (productionDate.isBefore(LocalDate.now())) {
            System.out.println("Error: Production date cannot be in the past.");
            return;
        }
        if (productionDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || productionDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            System.out.println("Error: Production cannot be scheduled on weekends.");
            return;
        }

        // ---- Step 6: Select priority ----
        System.out.println("\nSelect priority:");
        System.out.println("  1. LOW       (standard lead time)");
        System.out.println("  2. MEDIUM    (preferred scheduling)");
        System.out.println("  3. HIGH      (expedited, +20% cost)");
        System.out.println("  4. CRITICAL  (rush order, +50% cost)");
        System.out.print("Priority (1-4): ");
        ProductionBatch.Priority priority;
        String prioInput = scanner.nextLine().trim();
        switch (prioInput) {
            case "1" -> priority = ProductionBatch.Priority.LOW;
            case "2" -> priority = ProductionBatch.Priority.MEDIUM;
            case "3" -> priority = ProductionBatch.Priority.HIGH;
            case "4" -> priority = ProductionBatch.Priority.CRITICAL;
            default -> {
                System.out.println("Error: Invalid priority selection.");
                return;
            }
        }

        // ---- Step 7: Select production line with capacity and conflict checks ----
        System.out.println("\n--- Available Production Lines ---");
        List<ProductionLine> activeLines = new ArrayList<>();
        for (String line : FileManager.readLines(LINE_FILE)) {
            ProductionLine pl = ProductionLine.fromCSV(line);
            if (pl.getStatus() == ProductionLine.Status.ACTIVE) {
                activeLines.add(pl);
                int scheduled = countScheduledOnDate(pl.getId(), dateStr);
                int remaining = pl.getCapacityPerDay() - scheduled;
                System.out.println("  " + pl + " | Scheduled on " + dateStr + ": "
                                 + scheduled + " | Remaining capacity: " + remaining);
            }
        }
        if (activeLines.isEmpty()) {
            System.out.println("Error: No active production lines available.");
            return;
        }

        System.out.print("\nEnter Production Line ID: ");
        int lineId;
        try {
            lineId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Production Line ID must be a number.");
            return;
        }
        ProductionLine selectedLine = findLineById(lineId);
        if (selectedLine == null || selectedLine.getStatus() != ProductionLine.Status.ACTIVE) {
            System.out.println("Error: Production line not found or not active.");
            return;
        }

        // Capacity check
        int alreadyScheduled = countScheduledOnDate(lineId, dateStr);
        if (alreadyScheduled + batchQty > selectedLine.getCapacityPerDay()) {
            System.out.println("Error: Insufficient line capacity. Line " + selectedLine.getName()
                             + " has " + (selectedLine.getCapacityPerDay() - alreadyScheduled)
                             + " units remaining on " + dateStr + ", but batch requires " + batchQty + ".");
            return;
        }

        // Check for scheduling conflicts (same spec on same date)
        if (hasSchedulingConflict(specId, dateStr)) {
            System.out.println("Warning: Another batch for Spec ID " + specId
                             + " is already scheduled on " + dateStr + ".");
            System.out.print("Continue anyway? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Batch scheduling cancelled.");
                return;
            }
        }

        // ---- Step 8: Determine material requirements (BOM) ----
        System.out.println("\n--- Material Requirements Analysis ---");
        String fabricType = spec.getFabricType().toLowerCase();
        List<Material> matchingMaterials = findMaterialsByFabricType(fabricType);

        if (matchingMaterials.isEmpty()) {
            // Fallback: let user manually select materials
            System.out.println("No materials auto-matched for fabric type '" + spec.getFabricType() + "'.");
            System.out.println("Available materials:");
            List<String> matLines = FileManager.readLines(MaterialController.FILE);
            for (String ml : matLines) {
                System.out.println("  " + Material.fromCSV(ml));
            }
            System.out.print("Enter Material ID to use for this batch: ");
            int matId;
            try {
                matId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Material ID must be a number.");
                return;
            }
            Material mat = MaterialController.findById(matId);
            if (mat == null) {
                System.out.println("Error: Material not found.");
                return;
            }
            matchingMaterials = new ArrayList<>();
            matchingMaterials.add(mat);
        }

        // Calculate required quantities per material
        Map<Material, Integer> materialRequirements = new LinkedHashMap<>();
        int unitsPerMaterial = (int) Math.ceil(batchQty * MATERIAL_UNITS_PER_GARMENT / matchingMaterials.size());

        for (Material mat : matchingMaterials) {
            materialRequirements.put(mat, unitsPerMaterial);
        }

        // ---- Step 9: Check material availability ----
        boolean insufficientMaterials = false;
        System.out.println("\nMaterial availability check:");
        for (Map.Entry<Material, Integer> entry : materialRequirements.entrySet()) {
            Material mat = entry.getKey();
            int required = entry.getValue();
            String status = (mat.getStockLevel() >= required) ? "OK" : "INSUFFICIENT";
            System.out.println("  " + mat.getName() + " [ID:" + mat.getId() + "]"
                             + " | Required: " + required
                             + " | Available: " + mat.getStockLevel()
                             + " | " + status);
            if (mat.getStockLevel() < required) {
                insufficientMaterials = true;
            }
        }

        if (insufficientMaterials) {
            System.out.println("\nError: Insufficient materials. Cannot schedule batch.");
            System.out.println("Please place material orders or adjust batch quantity.");
            return;
        }

        // ---- Step 10: Calculate cost estimate ----
        double materialCost = 0;
        for (Map.Entry<Material, Integer> entry : materialRequirements.entrySet()) {
            materialCost += entry.getKey().getUnitCost() * entry.getValue();
        }
        double laborCost = batchQty * LABOR_COST_PER_UNIT;
        double subtotal = materialCost + laborCost;

        // Priority surcharge
        double priorityMultiplier = switch (priority) {
            case HIGH     -> 1.20;
            case CRITICAL -> 1.50;
            default       -> 1.00;
        };
        double totalCost = subtotal * priorityMultiplier * OVERHEAD_MULTIPLIER;

        // Estimated completion date (based on capacity)
        int daysNeeded = (int) Math.ceil((double) batchQty / selectedLine.getCapacityPerDay());
        LocalDate estCompletion = calculateCompletionDate(productionDate, daysNeeded);

        // ---- Step 11: Enter optional notes ----
        System.out.print("\nNotes (optional, press Enter to skip): ");
        String notes = scanner.nextLine().trim();
        if (notes.isEmpty()) notes = "none";

        // ---- Step 12: Display summary and confirm ----
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║        BATCH SCHEDULING SUMMARY          ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("  Specification ID : " + specId);
        System.out.println("  Fabric Type      : " + spec.getFabricType());
        System.out.println("  Sizes            : " + spec.getSizeRange());
        System.out.println("  Colors           : " + spec.getColorOptions());
        System.out.println("  Batch Quantity    : " + batchQty);
        System.out.println("  Production Date   : " + dateStr);
        System.out.println("  Est. Completion   : " + estCompletion.format(DATE_FMT));
        System.out.println("  Priority          : " + priority);
        System.out.println("  Production Line   : " + selectedLine.getName() + " (ID:" + lineId + ")");
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  Material Cost     : $" + String.format("%.2f", materialCost));
        System.out.println("  Labor Cost        : $" + String.format("%.2f", laborCost));
        if (priorityMultiplier > 1.0) {
            System.out.println("  Priority Surcharge: " + (int)((priorityMultiplier - 1) * 100) + "%");
        }
        System.out.println("  Overhead (15%)    : included");
        System.out.println("  TOTAL EST. COST   : $" + String.format("%.2f", totalCost));
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  Materials to deduct:");
        for (Map.Entry<Material, Integer> entry : materialRequirements.entrySet()) {
            System.out.println("    - " + entry.getKey().getName() + ": " + entry.getValue() + " units");
        }
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.print("\nConfirm scheduling? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Batch scheduling cancelled by user.");
            return;
        }

        // ---- Step 13: Deduct materials from stock ----
        for (Map.Entry<Material, Integer> entry : materialRequirements.entrySet()) {
            Material mat = entry.getKey();
            int deduction = entry.getValue();
            mat.setStockLevel(mat.getStockLevel() - deduction);
            MaterialController.update(mat);
        }

        // ---- Step 14: Create and save production batch ----
        int batchId = FileManager.nextId(BATCH_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        ProductionBatch batch = new ProductionBatch(
            batchId, specId, batchQty, dateStr,
            ProductionBatch.Status.SCHEDULED, priority,
            estCompletion.format(DATE_FMT), lineId, notes, totalCost, today
        );
        FileManager.appendLine(BATCH_FILE, batch.toCSV());

        // ---- Step 15: Save BOM records ----
        for (Map.Entry<Material, Integer> entry : materialRequirements.entrySet()) {
            int bomId = FileManager.nextId(BOM_FILE);
            BatchMaterialRequirement bom = new BatchMaterialRequirement(
                bomId, batchId, entry.getKey().getId(),
                entry.getValue(), entry.getValue(),
                BatchMaterialRequirement.Status.ALLOCATED
            );
            FileManager.appendLine(BOM_FILE, bom.toCSV());
        }

        // ---- Step 16: Confirmation ----
        System.out.println("\nProduction batch scheduled successfully!");
        System.out.println(batch);
        System.out.println("Materials have been deducted from inventory.");
    }

    // =========================================================================
    // 2. List All Batches
    // =========================================================================

    private void listAllBatches() {
        List<String> lines = FileManager.readLines(BATCH_FILE);
        if (lines.isEmpty()) {
            System.out.println("No production batches on file.");
            return;
        }
        System.out.println("\n--- All Production Batches ---");
        for (String line : lines) {
            System.out.println(ProductionBatch.fromCSV(line));
        }
        System.out.println("Total: " + lines.size() + " batch(es)");
    }

    // =========================================================================
    // 3. Filter Batches (by status, priority, or date range)
    // =========================================================================

    private void filterBatches() {
        System.out.println("\nFilter by:");
        System.out.println("  1. Status");
        System.out.println("  2. Priority");
        System.out.println("  3. Date Range");
        System.out.println("  4. Production Line");
        System.out.print("Select: ");
        String choice = scanner.nextLine().trim();

        List<ProductionBatch> all = loadAllBatches();
        if (all.isEmpty()) {
            System.out.println("No production batches on file.");
            return;
        }

        List<ProductionBatch> filtered;
        switch (choice) {
            case "1" -> {
                System.out.print("Enter status (SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED/ON_HOLD): ");
                String statusStr = scanner.nextLine().trim().toUpperCase();
                try {
                    ProductionBatch.Status status = ProductionBatch.Status.valueOf(statusStr);
                    filtered = all.stream().filter(b -> b.getStatus() == status).collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: Invalid status.");
                    return;
                }
            }
            case "2" -> {
                System.out.print("Enter priority (LOW/MEDIUM/HIGH/CRITICAL): ");
                String prioStr = scanner.nextLine().trim().toUpperCase();
                try {
                    ProductionBatch.Priority prio = ProductionBatch.Priority.valueOf(prioStr);
                    filtered = all.stream().filter(b -> b.getPriority() == prio).collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: Invalid priority.");
                    return;
                }
            }
            case "3" -> {
                System.out.print("Start date (YYYY-MM-DD): ");
                String startStr = scanner.nextLine().trim();
                System.out.print("End date (YYYY-MM-DD): ");
                String endStr = scanner.nextLine().trim();
                try {
                    LocalDate start = LocalDate.parse(startStr, DATE_FMT);
                    LocalDate end = LocalDate.parse(endStr, DATE_FMT);
                    filtered = all.stream().filter(b -> {
                        LocalDate d = LocalDate.parse(b.getProductionDate(), DATE_FMT);
                        return !d.isBefore(start) && !d.isAfter(end);
                    }).collect(Collectors.toList());
                } catch (DateTimeParseException e) {
                    System.out.println("Error: Invalid date format.");
                    return;
                }
            }
            case "4" -> {
                System.out.print("Enter Production Line ID: ");
                try {
                    int lid = Integer.parseInt(scanner.nextLine().trim());
                    filtered = all.stream().filter(b -> b.getProductionLineId() == lid).collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid ID.");
                    return;
                }
            }
            default -> {
                System.out.println("Invalid filter option.");
                return;
            }
        }

        if (filtered.isEmpty()) {
            System.out.println("No batches match the filter.");
        } else {
            System.out.println("\n--- Filtered Results (" + filtered.size() + " batch(es)) ---");
            for (ProductionBatch b : filtered) {
                System.out.println(b);
            }
        }
    }

    // =========================================================================
    // 4. View Batch Details (full breakdown)
    // =========================================================================

    private void viewBatchDetails() {
        System.out.print("Enter Batch ID: ");
        int batchId;
        try {
            batchId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Batch ID must be a number.");
            return;
        }

        ProductionBatch batch = findBatchById(batchId);
        if (batch == null) {
            System.out.println("Error: Batch not found.");
            return;
        }

        // Spec info
        ProductSpecification spec = ProductSpecificationController.findById(batch.getSpecId());
        GarmentDesign garment = (spec != null) ? GarmentDesignController.findById(spec.getGarmentDesignId()) : null;
        org.example.model.Collection collection = (garment != null) ? CollectionController.findById(garment.getCollectionId()) : null;
        ProductionLine line = findLineById(batch.getProductionLineId());

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║          BATCH DETAIL REPORT             ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("  Batch ID          : " + batch.getId());
        System.out.println("  Status            : " + batch.getStatus());
        System.out.println("  Priority          : " + batch.getPriority());
        System.out.println("  Batch Quantity     : " + batch.getBatchQuantity());
        System.out.println("  Production Date    : " + batch.getProductionDate());
        System.out.println("  Est. Completion    : " + batch.getEstimatedCompletionDate());
        System.out.println("  Total Cost         : $" + String.format("%.2f", batch.getTotalMaterialCost()));
        System.out.println("  Created            : " + batch.getCreatedDate());
        System.out.println("  Notes              : " + batch.getNotes());
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  Production Line    : " + (line != null ? line.getName() + " (" + line.getLocation() + ")" : "N/A"));
        System.out.println("  ──────────────────────────────────────");
        if (spec != null) {
            System.out.println("  Specification ID   : " + spec.getId());
            System.out.println("  Fabric Type        : " + spec.getFabricType());
            System.out.println("  Size Range         : " + spec.getSizeRange());
            System.out.println("  Colors             : " + spec.getColorOptions());
            System.out.println("  Measurements       : " + spec.getMeasurements());
        }
        if (garment != null) {
            System.out.println("  ──────────────────────────────────────");
            System.out.println("  Garment            : " + garment.getName() + " (" + garment.getType() + ")");
            System.out.println("  Style              : " + garment.getStyle());
            System.out.println("  Audience           : " + garment.getTargetAudience());
        }
        if (collection != null) {
            System.out.println("  Collection         : " + collection.getName() + " | " + collection.getSeason());
        }

        // BOM details
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  Material Requirements (BOM):");
        List<BatchMaterialRequirement> boms = loadBOMForBatch(batchId);
        if (boms.isEmpty()) {
            System.out.println("    (no BOM records)");
        } else {
            for (BatchMaterialRequirement bom : boms) {
                Material mat = MaterialController.findById(bom.getMaterialId());
                String matName = (mat != null) ? mat.getName() : "Unknown";
                System.out.println("    - " + matName + " [ID:" + bom.getMaterialId()
                                 + "] | Required: " + bom.getRequiredQuantity()
                                 + " | Allocated: " + bom.getAllocatedQuantity()
                                 + " | Status: " + bom.getStatus());
            }
        }
        System.out.println("╚══════════════════════════════════════════╝");
    }

    // =========================================================================
    // 5. Cancel Batch (restore materials to stock)
    // =========================================================================

    private void cancelBatch() {
        System.out.print("Enter Batch ID to cancel: ");
        int batchId;
        try {
            batchId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Batch ID must be a number.");
            return;
        }

        ProductionBatch batch = findBatchById(batchId);
        if (batch == null) {
            System.out.println("Error: Batch not found.");
            return;
        }
        if (batch.getStatus() == ProductionBatch.Status.CANCELLED) {
            System.out.println("Error: Batch is already cancelled.");
            return;
        }
        if (batch.getStatus() == ProductionBatch.Status.COMPLETED) {
            System.out.println("Error: Cannot cancel a completed batch.");
            return;
        }

        System.out.println("Batch to cancel: " + batch);
        System.out.print("Are you sure you want to cancel this batch? Materials will be restored. (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        // Restore materials
        List<BatchMaterialRequirement> boms = loadBOMForBatch(batchId);
        for (BatchMaterialRequirement bom : boms) {
            if (bom.getStatus() == BatchMaterialRequirement.Status.ALLOCATED) {
                Material mat = MaterialController.findById(bom.getMaterialId());
                if (mat != null) {
                    mat.setStockLevel(mat.getStockLevel() + bom.getAllocatedQuantity());
                    MaterialController.update(mat);
                }
                bom.setStatus(BatchMaterialRequirement.Status.RETURNED);
                bom.setAllocatedQuantity(0);
                updateBOM(bom);
            }
        }

        // Update batch status
        batch.setStatus(ProductionBatch.Status.CANCELLED);
        updateBatch(batch);

        System.out.println("Batch " + batchId + " cancelled. Materials restored to inventory.");
    }

    // =========================================================================
    // 6. Hold / Resume Batch
    // =========================================================================

    private void holdResumeBatch() {
        System.out.print("Enter Batch ID: ");
        int batchId;
        try {
            batchId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Batch ID must be a number.");
            return;
        }

        ProductionBatch batch = findBatchById(batchId);
        if (batch == null) {
            System.out.println("Error: Batch not found.");
            return;
        }

        if (batch.getStatus() == ProductionBatch.Status.ON_HOLD) {
            System.out.println("Batch is currently ON HOLD. Resuming to SCHEDULED...");
            batch.setStatus(ProductionBatch.Status.SCHEDULED);
            updateBatch(batch);
            System.out.println("Batch " + batchId + " resumed. Status: SCHEDULED");
        } else if (batch.getStatus() == ProductionBatch.Status.SCHEDULED
                || batch.getStatus() == ProductionBatch.Status.IN_PROGRESS) {
            System.out.println("Putting batch ON HOLD...");
            System.out.print("Reason for hold: ");
            String reason = scanner.nextLine().trim();
            batch.setStatus(ProductionBatch.Status.ON_HOLD);
            batch.setNotes(batch.getNotes() + " | HOLD: " + reason);
            updateBatch(batch);
            System.out.println("Batch " + batchId + " placed on hold.");
        } else {
            System.out.println("Error: Batch status " + batch.getStatus() + " cannot be held/resumed.");
        }
    }

    // =========================================================================
    // 7. Register Production Line
    // =========================================================================

    private void registerProductionLine() {
        System.out.println("\n--- Register Production Line ---");

        System.out.print("Line name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Capacity per day (units): ");
        int capacity;
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Capacity must be a whole number.");
            return;
        }
        if (capacity <= 0) {
            System.out.println("Error: Capacity must be greater than 0.");
            return;
        }

        System.out.print("Location (e.g. Building A - Floor 2): ");
        String location = scanner.nextLine().trim();

        if (name.isEmpty() || location.isEmpty()) {
            System.out.println("Error: Name and location are required.");
            return;
        }

        // Check duplicate name
        for (String line : FileManager.readLines(LINE_FILE)) {
            ProductionLine existing = ProductionLine.fromCSV(line);
            if (existing.getName().equalsIgnoreCase(name)) {
                System.out.println("Error: A production line with that name already exists.");
                return;
            }
        }

        int id = FileManager.nextId(LINE_FILE);
        ProductionLine pl = new ProductionLine(id, name, capacity, ProductionLine.Status.ACTIVE, location);
        FileManager.appendLine(LINE_FILE, pl.toCSV());
        System.out.println("Production line registered: " + pl);
    }

    // =========================================================================
    // 8. List Production Lines
    // =========================================================================

    private void listProductionLines() {
        List<String> lines = FileManager.readLines(LINE_FILE);
        if (lines.isEmpty()) {
            System.out.println("No production lines registered.");
            return;
        }
        System.out.println("\n--- Production Lines ---");
        for (String line : lines) {
            ProductionLine pl = ProductionLine.fromCSV(line);
            System.out.println(pl);
        }
    }

    // =========================================================================
    // 9. Production Calendar (shows batches by date)
    // =========================================================================

    private void productionCalendar() {
        List<ProductionBatch> batches = loadAllBatches();
        if (batches.isEmpty()) {
            System.out.println("No batches scheduled.");
            return;
        }

        System.out.print("Enter month to view (YYYY-MM): ");
        String monthStr = scanner.nextLine().trim();
        int year, month;
        try {
            String[] parts = monthStr.split("-");
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            if (month < 1 || month > 12) throw new NumberFormatException();
        } catch (Exception e) {
            System.out.println("Error: Invalid month format. Use YYYY-MM.");
            return;
        }

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║     PRODUCTION CALENDAR: " + firstDay.getMonth() + " " + year + "          ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        LocalDate current = firstDay;
        while (!current.isAfter(lastDay)) {
            final LocalDate date = current;
            List<ProductionBatch> dayBatches = batches.stream()
                .filter(b -> b.getProductionDate().equals(date.format(DATE_FMT)))
                .filter(b -> b.getStatus() != ProductionBatch.Status.CANCELLED)
                .collect(Collectors.toList());

            if (!dayBatches.isEmpty()) {
                String dayLabel = date.getDayOfWeek().toString().substring(0, 3) + " " + date.format(DATE_FMT);
                System.out.println("  " + dayLabel + ":");
                for (ProductionBatch b : dayBatches) {
                    ProductionLine line = findLineById(b.getProductionLineId());
                    String lineName = (line != null) ? line.getName() : "?";
                    System.out.println("    Batch #" + b.getId() + " | Qty: " + b.getBatchQuantity()
                                     + " | " + b.getPriority() + " | " + b.getStatus()
                                     + " | Line: " + lineName);
                }
            }
            current = current.plusDays(1);
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 10. Batch Statistics Dashboard
    // =========================================================================

    private void batchStatisticsDashboard() {
        List<ProductionBatch> batches = loadAllBatches();
        if (batches.isEmpty()) {
            System.out.println("No batches to analyze.");
            return;
        }

        int total = batches.size();
        long scheduled = batches.stream().filter(b -> b.getStatus() == ProductionBatch.Status.SCHEDULED).count();
        long inProgress = batches.stream().filter(b -> b.getStatus() == ProductionBatch.Status.IN_PROGRESS).count();
        long completed = batches.stream().filter(b -> b.getStatus() == ProductionBatch.Status.COMPLETED).count();
        long cancelled = batches.stream().filter(b -> b.getStatus() == ProductionBatch.Status.CANCELLED).count();
        long onHold = batches.stream().filter(b -> b.getStatus() == ProductionBatch.Status.ON_HOLD).count();

        int totalUnits = batches.stream()
            .filter(b -> b.getStatus() != ProductionBatch.Status.CANCELLED)
            .mapToInt(ProductionBatch::getBatchQuantity).sum();

        double totalCost = batches.stream()
            .filter(b -> b.getStatus() != ProductionBatch.Status.CANCELLED)
            .mapToDouble(ProductionBatch::getTotalMaterialCost).sum();

        double avgBatchSize = batches.stream()
            .filter(b -> b.getStatus() != ProductionBatch.Status.CANCELLED)
            .mapToInt(ProductionBatch::getBatchQuantity).average().orElse(0);

        long critical = batches.stream().filter(b -> b.getPriority() == ProductionBatch.Priority.CRITICAL).count();
        long high = batches.stream().filter(b -> b.getPriority() == ProductionBatch.Priority.HIGH).count();
        long medium = batches.stream().filter(b -> b.getPriority() == ProductionBatch.Priority.MEDIUM).count();
        long low = batches.stream().filter(b -> b.getPriority() == ProductionBatch.Priority.LOW).count();

        // Line utilization
        Map<Integer, Integer> lineLoad = new HashMap<>();
        for (ProductionBatch b : batches) {
            if (b.getStatus() != ProductionBatch.Status.CANCELLED) {
                lineLoad.merge(b.getProductionLineId(), b.getBatchQuantity(), Integer::sum);
            }
        }

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       PRODUCTION BATCH STATISTICS DASHBOARD      ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("  Total Batches      : " + total);
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  STATUS BREAKDOWN:");
        System.out.println("    Scheduled        : " + scheduled);
        System.out.println("    In Progress      : " + inProgress);
        System.out.println("    Completed        : " + completed);
        System.out.println("    Cancelled        : " + cancelled);
        System.out.println("    On Hold          : " + onHold);
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  PRIORITY BREAKDOWN:");
        System.out.println("    Critical         : " + critical);
        System.out.println("    High             : " + high);
        System.out.println("    Medium           : " + medium);
        System.out.println("    Low              : " + low);
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  PRODUCTION METRICS:");
        System.out.println("    Total Units      : " + totalUnits);
        System.out.println("    Avg Batch Size   : " + String.format("%.1f", avgBatchSize));
        System.out.println("    Total Est. Cost  : $" + String.format("%.2f", totalCost));
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  LINE UTILIZATION (total units assigned):");
        for (Map.Entry<Integer, Integer> entry : lineLoad.entrySet()) {
            ProductionLine pl = findLineById(entry.getKey());
            String lineName = (pl != null) ? pl.getName() : "Line " + entry.getKey();
            System.out.println("    " + lineName + " : " + entry.getValue() + " units");
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private List<ProductionBatch> loadAllBatches() {
        List<ProductionBatch> batches = new ArrayList<>();
        for (String line : FileManager.readLines(BATCH_FILE)) {
            batches.add(ProductionBatch.fromCSV(line));
        }
        return batches;
    }

    private ProductionBatch findBatchById(int id) {
        for (String line : FileManager.readLines(BATCH_FILE)) {
            ProductionBatch b = ProductionBatch.fromCSV(line);
            if (b.getId() == id) return b;
        }
        return null;
    }

    private void updateBatch(ProductionBatch updated) {
        List<String> lines = FileManager.readLines(BATCH_FILE);
        for (int i = 0; i < lines.size(); i++) {
            ProductionBatch b = ProductionBatch.fromCSV(lines.get(i));
            if (b.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(BATCH_FILE, lines);
    }

    private ProductionLine findLineById(int id) {
        for (String line : FileManager.readLines(LINE_FILE)) {
            ProductionLine pl = ProductionLine.fromCSV(line);
            if (pl.getId() == id) return pl;
        }
        return null;
    }

    private List<BatchMaterialRequirement> loadBOMForBatch(int batchId) {
        List<BatchMaterialRequirement> result = new ArrayList<>();
        for (String line : FileManager.readLines(BOM_FILE)) {
            BatchMaterialRequirement bom = BatchMaterialRequirement.fromCSV(line);
            if (bom.getBatchId() == batchId) result.add(bom);
        }
        return result;
    }

    private void updateBOM(BatchMaterialRequirement updated) {
        List<String> lines = FileManager.readLines(BOM_FILE);
        for (int i = 0; i < lines.size(); i++) {
            BatchMaterialRequirement bom = BatchMaterialRequirement.fromCSV(lines.get(i));
            if (bom.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(BOM_FILE, lines);
    }

    private int countScheduledOnDate(int lineId, String date) {
        int count = 0;
        for (String line : FileManager.readLines(BATCH_FILE)) {
            ProductionBatch b = ProductionBatch.fromCSV(line);
            if (b.getProductionLineId() == lineId
                    && b.getProductionDate().equals(date)
                    && b.getStatus() != ProductionBatch.Status.CANCELLED) {
                count += b.getBatchQuantity();
            }
        }
        return count;
    }

    private boolean hasSchedulingConflict(int specId, String date) {
        for (String line : FileManager.readLines(BATCH_FILE)) {
            ProductionBatch b = ProductionBatch.fromCSV(line);
            if (b.getSpecId() == specId
                    && b.getProductionDate().equals(date)
                    && b.getStatus() != ProductionBatch.Status.CANCELLED) {
                return true;
            }
        }
        return false;
    }

    private List<Material> findMaterialsByFabricType(String fabricType) {
        List<Material> matches = new ArrayList<>();
        for (String line : FileManager.readLines(MaterialController.FILE)) {
            Material m = Material.fromCSV(line);
            if (m.getName().toLowerCase().contains(fabricType)
                    || m.getCategory().toLowerCase().contains(fabricType)) {
                matches.add(m);
            }
        }
        return matches;
    }

    private LocalDate calculateCompletionDate(LocalDate start, int workingDays) {
        LocalDate date = start;
        int daysAdded = 0;
        while (daysAdded < workingDays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                daysAdded++;
            }
        }
        return date;
    }

    // =========================================================================
    // Static helpers for other controllers
    // =========================================================================

    public static ProductionBatch findById(int id) {
        for (String line : FileManager.readLines(BATCH_FILE)) {
            ProductionBatch b = ProductionBatch.fromCSV(line);
            if (b.getId() == id) return b;
        }
        return null;
    }

    public static void update(ProductionBatch updated) {
        List<String> lines = FileManager.readLines(BATCH_FILE);
        for (int i = 0; i < lines.size(); i++) {
            ProductionBatch b = ProductionBatch.fromCSV(lines.get(i));
            if (b.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(BATCH_FILE, lines);
    }
}
