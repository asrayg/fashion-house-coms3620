package org.example.model;

/**
 * Sellable retail item for Sales and Retail use cases.
 * CSV format: id,sku,name,category,unitPrice,stockQuantity
 */
public class RetailItem {

    private int id;
    private String sku;
    private String name;
    private String category;
    private double unitPrice;
    private int stockQuantity;

    public RetailItem(int id, String sku, String name, String category, double unitPrice, int stockQuantity) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    public static RetailItem fromCSV(String line) {
        String[] parts = line.split(",", 6);
        return new RetailItem(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            parts[3].trim(),
            Double.parseDouble(parts[4].trim()),
            Integer.parseInt(parts[5].trim())
        );
    }

    public String toCSV() {
        return id + "," + sku + "," + name + "," + category + "," + unitPrice + "," + stockQuantity;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + sku + " | " + name + " | Category: " + category +
            " | Price: $" + String.format("%.2f", unitPrice) + " | Stock: " + stockQuantity;
    }

    public int getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
