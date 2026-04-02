package org.example.model;

/**
 * Technical specification for a garment design.
 * UC3: Define Product Specification
 *
 * CSV format: id,garmentId,sizeRange,colorOptions,fabricType,measurements
 */
public class ProductSpecification {

    private int id;
    private int garmentId;
    private String sizeRange;
    private String colorOptions;
    private String fabricType;
    private String measurements;

    public ProductSpecification(int id, int garmentId, String sizeRange,
                                String colorOptions, String fabricType, String measurements) {
        this.id = id;
        this.garmentId = garmentId;
        this.sizeRange = sizeRange;
        this.colorOptions = colorOptions;
        this.fabricType = fabricType;
        this.measurements = measurements;
    }

    public String toCSV() {
        return id + "," + garmentId + "," + sizeRange + "," +
               colorOptions + "," + fabricType + "," + measurements;
    }

    public static ProductSpecification fromCSV(String line) {
        String[] parts = line.split(",", 6);
        return new ProductSpecification(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            parts[5].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] GarmentID: " + garmentId + " | Sizes: " + sizeRange +
               " | Colors: " + colorOptions + " | Fabric: " + fabricType +
               " | Measurements: " + measurements;
    }

    // --- Getters ---
    public int getId()             { return id; }
    public int getGarmentId()      { return garmentId; }
    public String getSizeRange()   { return sizeRange; }
    public String getColorOptions() { return colorOptions; }
    public String getFabricType()  { return fabricType; }
    public String getMeasurements() { return measurements; }
}
