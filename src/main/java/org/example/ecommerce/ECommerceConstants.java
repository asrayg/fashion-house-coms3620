package org.example.ecommerce;

/**
 * Shared constants for the e-commerce module (orders file path, pricing, inventory model).
 */
public final class ECommerceConstants {

    private ECommerceConstants() {}

    public static final String ORDERS_FILE = "data/orders.csv";
    public static final int DELIVERY_DAYS = 7;
    public static final double DEFAULT_UNIT_PRICE = 50.00;
    public static final int BASE_STOCK_PER_SPEC = 100;
}
