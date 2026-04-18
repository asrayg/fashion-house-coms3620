package org.example.model;

/**
 * Tracks material requirements for a specific production batch (Bill of Materials).
 * Each batch may require multiple materials, each tracked separately.
 *
 * CSV format: id,batchId,materialId,requiredQuantity,allocatedQuantity,status
 */
public class BatchMaterialRequirement {

    public enum Status { PENDING, ALLOCATED, CONSUMED, RETURNED }

    private int id;
    private int batchId;
    private int materialId;
    private int requiredQuantity;
    private int allocatedQuantity;
    private Status status;

    public BatchMaterialRequirement(int id, int batchId, int materialId,
                                     int requiredQuantity, int allocatedQuantity,
                                     Status status) {
        this.id = id;
        this.batchId = batchId;
        this.materialId = materialId;
        this.requiredQuantity = requiredQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.status = status;
    }

    public String toCSV() {
        return id + "," + batchId + "," + materialId + "," + requiredQuantity
             + "," + allocatedQuantity + "," + status.name();
    }

    public static BatchMaterialRequirement fromCSV(String line) {
        String[] parts = line.split(",", 6);
        return new BatchMaterialRequirement(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim()),
            Integer.parseInt(parts[3].trim()),
            Integer.parseInt(parts[4].trim()),
            Status.valueOf(parts[5].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] BatchID: " + batchId + " | MaterialID: " + materialId
             + " | Required: " + requiredQuantity + " | Allocated: " + allocatedQuantity
             + " | Status: " + status;
    }

    // --- Getters ---
    public int getId()               { return id; }
    public int getBatchId()          { return batchId; }
    public int getMaterialId()       { return materialId; }
    public int getRequiredQuantity() { return requiredQuantity; }
    public int getAllocatedQuantity() { return allocatedQuantity; }
    public Status getStatus()        { return status; }

    // --- Setters ---
    public void setAllocatedQuantity(int qty) { this.allocatedQuantity = qty; }
    public void setStatus(Status status)      { this.status = status; }
}
