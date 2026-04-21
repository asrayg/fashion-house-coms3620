package org.example.sales.model;

/**
 * Line item in a sale.
 * Encoded format: itemId~sku~name~unitPrice~quantity
 */
public class SaleLine {

    private int itemId;
    private String sku;
    private String itemName;
    private double unitPrice;
    private int quantity;

    public SaleLine(int itemId, String sku, String itemName, double unitPrice, int quantity) {
        this.itemId = itemId;
        this.sku = sku;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String encode() {
        return itemId + "~" + sku + "~" + itemName + "~" + unitPrice + "~" + quantity;
    }

    public static SaleLine decode(String value) {
        String[] parts = value.split("~", 5);
        return new SaleLine(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            Double.parseDouble(parts[3].trim()),
            Integer.parseInt(parts[4].trim())
        );
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }

    public int getItemId() {
        return itemId;
    }

    public String getSku() {
        return sku;
    }

    public String getItemName() {
        return itemName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
