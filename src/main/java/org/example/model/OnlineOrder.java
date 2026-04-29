package org.example.model;

/**
 * Represents a customer online order linked to a product specification.
 * Supports full order lifecycle: PENDING → CONFIRMED → SHIPPED → DELIVERED
 * Soft cancellation and refund supported via status transitions.
 *
 * CSV format:
 * id,customerName,customerEmail,specId,quantity,unitPrice,totalPrice,
 * shippingAddress,paymentMethod,orderDate,estimatedDelivery,status,notes
 */
public class OnlineOrder {

    public enum Status {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    private int id;
    private String customerName;
    private String customerEmail;
    private int specId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String shippingAddress;
    private String paymentMethod;
    private String orderDate;
    private String estimatedDelivery;
    private Status status;
    private String notes;

    public OnlineOrder(int id, String customerName, String customerEmail,
                       int specId, int quantity, double unitPrice, double totalPrice,
                       String shippingAddress, String paymentMethod,
                       String orderDate, String estimatedDelivery,
                       Status status, String notes) {
        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.specId = specId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
        this.estimatedDelivery = estimatedDelivery;
        this.status = status;
        this.notes = notes;
    }

    public double calculateTotal() {
        return unitPrice * quantity;
    }

    public String toCSV() {
        return id + "," + customerName + "," + customerEmail + ","
                + specId + "," + quantity + "," + unitPrice + ","
                + totalPrice + "," + shippingAddress + ","
                + paymentMethod + "," + orderDate + ","
                + estimatedDelivery + "," + status.name() + "," + notes;
    }

    public static OnlineOrder fromCSV(String line) {
        String[] p = line.split(",", 13);
        return new OnlineOrder(
            Integer.parseInt(p[0].trim()),
            p[1].trim(), p[2].trim(),
            Integer.parseInt(p[3].trim()),
            Integer.parseInt(p[4].trim()),
            Double.parseDouble(p[5].trim()),
            Double.parseDouble(p[6].trim()),
            p[7].trim(), p[8].trim(),
            p[9].trim(), p[10].trim(),
            Status.valueOf(p[11].trim()),
            p.length > 12 ? p[12].trim() : ""
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + customerName + " <" + customerEmail + ">"
                + "\n  Spec: " + specId + " | Qty: " + quantity
                + " | Unit: $" + String.format("%.2f", unitPrice)
                + " | Total: $" + String.format("%.2f", totalPrice)
                + "\n  Ship to: " + shippingAddress
                + " | Payment: " + paymentMethod
                + "\n  Ordered: " + orderDate
                + " | Est. Delivery: " + estimatedDelivery
                + " | Status: " + status
                + (notes.isEmpty() ? "" : "\n  Notes: " + notes);
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public int getSpecId() { return specId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderDate() { return orderDate; }
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }

    public void setStatus(Status status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setEstimatedDelivery(String d) { this.estimatedDelivery = d; }
}
