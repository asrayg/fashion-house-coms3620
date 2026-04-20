package org.example.model;

public class MaterialConsumption {
    private int id;
    private int allocationId;
    private int materialId;
    private int allocatedQuantity;
    private int actualQuantity;
    private int wasteQuantity;
    private int unitsProduced;
    private String consumptionDate;
    private String notes;

    public MaterialConsumption(int id, int allocationId, int materialId, int allocatedQuantity,
                              int actualQuantity, int wasteQuantity, int unitsProduced,
                              String consumptionDate, String notes) {
        this.id = id;
        this.allocationId = allocationId;
        this.materialId = materialId;
        this.allocatedQuantity = allocatedQuantity;
        this.actualQuantity = actualQuantity;
        this.wasteQuantity = wasteQuantity;
        this.unitsProduced = unitsProduced;
        this.consumptionDate = consumptionDate;
        this.notes = notes;
    }

    public String toCSV() {
        return id + "," + allocationId + "," + materialId + "," + allocatedQuantity + "," +
               actualQuantity + "," + wasteQuantity + "," + unitsProduced + "," +
               consumptionDate + "," + notes;
    }

    public static MaterialConsumption fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new MaterialConsumption(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4]),
            Integer.parseInt(parts[5]),
            Integer.parseInt(parts[6]),
            parts[7],
            parts.length > 8 ? parts[8] : ""
        );
    }

    public int getId() { return id; }
    public int getAllocationId() { return allocationId; }
    public int getMaterialId() { return materialId; }
    public int getAllocatedQuantity() { return allocatedQuantity; }
    public int getActualQuantity() { return actualQuantity; }
    public int getWasteQuantity() { return wasteQuantity; }
    public int getUnitsProduced() { return unitsProduced; }
    public String getConsumptionDate() { return consumptionDate; }
    public String getNotes() { return notes; }

    public double getWastePercentage() {
        if (actualQuantity == 0) return 0;
        return (wasteQuantity * 100.0) / actualQuantity;
    }

    public double getEfficiencyRate() {
        if (actualQuantity == 0) return 0;
        return ((actualQuantity - wasteQuantity) * 100.0) / actualQuantity;
    }

    @Override
    public String toString() {
        return "MaterialConsumption{id=" + id + ", allocationId=" + allocationId +
               ", materialId=" + materialId + ", waste=" + String.format("%.2f", getWastePercentage()) + "%}";
    }
}