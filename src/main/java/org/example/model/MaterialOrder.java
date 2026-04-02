package org.example.model;

/**
 * A purchase order placed for a material from a supplier.
 * UC5: Place Material Order
 *
 * CSV format: id,materialId,supplierName,quantity,expectedDelivery,status
 * Status values: PENDING | RECEIVED | PARTIALLY_RECEIVED
 */
public class MaterialOrder {

    public enum Status { PENDING, RECEIVED, PARTIALLY_RECEIVED }

    private int id;
    private int materialId;
    private String supplierName;
    private int quantity;
    private String expectedDelivery;
    private Status status;

    public MaterialOrder(int id, int materialId, String supplierName,
                         int quantity, String expectedDelivery, Status status) {
        this.id = id;
        this.materialId = materialId;
        this.supplierName = supplierName;
        this.quantity = quantity;
        this.expectedDelivery = expectedDelivery;
        this.status = status;
    }

    public String toCSV() {
        return id + "," + materialId + "," + supplierName + "," +
               quantity + "," + expectedDelivery + "," + status.name();
    }

    public static MaterialOrder fromCSV(String line) {
        String[] parts = line.split(",", 6);
        return new MaterialOrder(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim()),
            parts[4].trim(),
            Status.valueOf(parts[5].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] MaterialID: " + materialId + " | Supplier: " + supplierName +
               " | Qty: " + quantity + " | Expected: " + expectedDelivery +
               " | Status: " + status;
    }

    // --- Getters ---
    public int getId()               { return id; }
    public int getMaterialId()       { return materialId; }
    public String getSupplierName()  { return supplierName; }
    public int getQuantity()         { return quantity; }
    public String getExpectedDelivery() { return expectedDelivery; }
    public Status getStatus()        { return status; }

    public void setStatus(Status status) { this.status = status; }
}
