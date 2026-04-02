package org.example.model;

/**
 * A raw material used in garment production.
 * UC4: Register Material
 *
 * CSV format: id,name,category,unitCost,stockLevel
 */
public class Material {

    private int id;
    private String name;
    private String category;
    private double unitCost;
    private int stockLevel;

    public Material(int id, String name, String category, double unitCost, int stockLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unitCost = unitCost;
        this.stockLevel = stockLevel;
    }

    public String toCSV() {
        return id + "," + name + "," + category + "," + unitCost + "," + stockLevel;
    }

    public static Material fromCSV(String line) {
        String[] parts = line.split(",", 5);
        return new Material(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            Double.parseDouble(parts[3].trim()),
            Integer.parseInt(parts[4].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Category: " + category +
               " | Unit Cost: $" + unitCost + " | Stock: " + stockLevel;
    }

    // --- Getters ---
    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getUnitCost() { return unitCost; }
    public int getStockLevel()  { return stockLevel; }

    // Setter for stock (used when shipments arrive)
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
}
