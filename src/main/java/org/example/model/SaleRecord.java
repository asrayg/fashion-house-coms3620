package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent sale transaction for reporting.
 * File format uses pipe separators to keep list data in one line.
 */
public class SaleRecord {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private int id;
    private LocalDateTime timestamp;
    private String paymentMethod;
    private String discountType;
    private double discountValue;
    private double subtotal;
    private double total;
    private String status;
    private List<SaleLine> lines;

    public SaleRecord(int id, LocalDateTime timestamp, String paymentMethod, String discountType,
                      double discountValue, double subtotal, double total, String status,
                      List<SaleLine> lines) {
        this.id = id;
        this.timestamp = timestamp;
        this.paymentMethod = paymentMethod;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.subtotal = subtotal;
        this.total = total;
        this.status = status;
        this.lines = lines;
    }

    public String toStorageLine() {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) items.append(";");
            items.append(lines.get(i).encode());
        }
        return id + "|" + TS_FORMAT.format(timestamp) + "|" + paymentMethod + "|" +
            discountType + "|" + discountValue + "|" + subtotal + "|" + total + "|" + status +
            "|" + items;
    }

    public static SaleRecord fromStorageLine(String line) {
        String[] parts = line.split("\\|", 9);
        List<SaleLine> decoded = new ArrayList<>();
        if (parts.length > 8 && !parts[8].trim().isEmpty()) {
            String[] lineParts = parts[8].split(";");
            for (String linePart : lineParts) {
                decoded.add(SaleLine.decode(linePart));
            }
        }
        return new SaleRecord(
            Integer.parseInt(parts[0].trim()),
            LocalDateTime.parse(parts[1].trim(), TS_FORMAT),
            parts[2].trim(),
            parts[3].trim(),
            Double.parseDouble(parts[4].trim()),
            Double.parseDouble(parts[5].trim()),
            Double.parseDouble(parts[6].trim()),
            parts[7].trim(),
            decoded
        );
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public List<SaleLine> getLines() {
        return lines;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
