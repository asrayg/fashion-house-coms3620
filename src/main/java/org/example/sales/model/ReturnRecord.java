package org.example.sales.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Return/refund transaction for a specific sale line.
 * Storage format: id|saleId|itemId|sku|quantity|refundAmount|timestamp
 */
public class ReturnRecord {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private int id;
    private int saleId;
    private int itemId;
    private String sku;
    private int quantity;
    private double refundAmount;
    private LocalDateTime timestamp;

    public ReturnRecord(int id, int saleId, int itemId, String sku, int quantity,
                        double refundAmount, LocalDateTime timestamp) {
        this.id = id;
        this.saleId = saleId;
        this.itemId = itemId;
        this.sku = sku;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
        this.timestamp = timestamp;
    }

    public String toStorageLine() {
        return id + "|" + saleId + "|" + itemId + "|" + sku + "|" + quantity + "|" +
            refundAmount + "|" + TS_FORMAT.format(timestamp);
    }

    public static ReturnRecord fromStorageLine(String line) {
        String[] parts = line.split("\\|", 7);
        return new ReturnRecord(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim()),
            parts[3].trim(),
            Integer.parseInt(parts[4].trim()),
            Double.parseDouble(parts[5].trim()),
            LocalDateTime.parse(parts[6].trim(), TS_FORMAT)
        );
    }

    public int getSaleId() {
        return saleId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
