package org.example.model;

/**
 * Represents a product specification linked to a garment design.
 * UC3: Define Product Specification
 *
 * CSV format: id,garmentDesignId,sizeRange,colorOptions,fabricType,measurements
 */
public class ProductSpecification {

    private int id;
    private int garmentDesignId;
    private String sizeRange;
    private String colorOptions;
    private String fabricType;
    private String measurements;

    public ProductSpecification(int id, int garmentDesignId, String sizeRange,
                                String colorOptions, String fabricType, String measurements) {
        this.id = id;
        this.garmentDesignId = garmentDesignId;
        this.sizeRange = sizeRange;
        this.colorOptions = colorOptions;
        this.fabricType = fabricType;
        this.measurements = measurements;
    }

    /** Serialize to a single CSV line. */
    public String toCSV() {
        return id + "," + garmentDesignId + "," + sizeRange + ","
                + colorOptions + "," + fabricType + "," + measurements;
    }

    /** Deserialize from a single CSV line. */
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
        return "[" + id + "] Design ID: " + garmentDesignId
                + " | Sizes: " + sizeRange
                + " | Colors: " + colorOptions
                + " | Fabric: " + fabricType
                + " | Measurements: " + measurements;
    }

    public int getId()               { return id; }
    public int getGarmentDesignId()  { return garmentDesignId; }
    public String getSizeRange()     { return sizeRange; }
    public String getColorOptions()  { return colorOptions; }
    public String getFabricType()    { return fabricType; }
    public String getMeasurements()  { return measurements; }
}
