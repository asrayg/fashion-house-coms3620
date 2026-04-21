package org.example.model;

/**
 * Represents a production batch linked to a product specification.
 * UC6 (Iteration 2): Schedule Production Batch
 *
 * CSV format: id,specId,batchQuantity,productionDate,status,priority,
 *             estimatedCompletionDate,productionLineId,notes,totalMaterialCost,createdDate
 */
public class ProductionBatch {

    public enum Status { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    private int id;
    private int specId;
    private int batchQuantity;
    private String productionDate;
    private Status status;
    private Priority priority;
    private String estimatedCompletionDate;
    private int productionLineId;
    private String notes;
    private double totalMaterialCost;
    private String createdDate;

    public ProductionBatch(int id, int specId, int batchQuantity, String productionDate,
                           Status status, Priority priority, String estimatedCompletionDate,
                           int productionLineId, String notes, double totalMaterialCost,
                           String createdDate) {
        this.id = id;
        this.specId = specId;
        this.batchQuantity = batchQuantity;
        this.productionDate = productionDate;
        this.status = status;
        this.priority = priority;
        this.estimatedCompletionDate = estimatedCompletionDate;
        this.productionLineId = productionLineId;
        this.notes = notes;
        this.totalMaterialCost = totalMaterialCost;
        this.createdDate = createdDate;
    }

    public String toCSV() {
        return id + "," + specId + "," + batchQuantity + "," + productionDate + ","
             + status.name() + "," + priority.name() + "," + estimatedCompletionDate + ","
             + productionLineId + "," + notes + "," + totalMaterialCost + "," + createdDate;
    }

    public static ProductionBatch fromCSV(String line) {
        String[] parts = line.split(",", 11);
        return new ProductionBatch(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim()),
            parts[3].trim(),
            Status.valueOf(parts[4].trim()),
            Priority.valueOf(parts[5].trim()),
            parts[6].trim(),
            Integer.parseInt(parts[7].trim()),
            parts[8].trim(),
            Double.parseDouble(parts[9].trim()),
            parts[10].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] SpecID: " + specId + " | Qty: " + batchQuantity
             + " | Date: " + productionDate + " | Status: " + status
             + " | Priority: " + priority + " | Line: " + productionLineId
             + " | Est. Completion: " + estimatedCompletionDate
             + " | Cost: $" + String.format("%.2f", totalMaterialCost)
             + " | Created: " + createdDate;
    }

    // --- Getters ---
    public int getId()                      { return id; }
    public int getSpecId()                  { return specId; }
    public int getBatchQuantity()           { return batchQuantity; }
    public String getProductionDate()       { return productionDate; }
    public Status getStatus()               { return status; }
    public Priority getPriority()           { return priority; }
    public String getEstimatedCompletionDate() { return estimatedCompletionDate; }
    public int getProductionLineId()        { return productionLineId; }
    public String getNotes()                { return notes; }
    public double getTotalMaterialCost()    { return totalMaterialCost; }
    public String getCreatedDate()          { return createdDate; }

    // --- Setters ---
    public void setStatus(Status status)    { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setNotes(String notes)      { this.notes = notes; }
}
