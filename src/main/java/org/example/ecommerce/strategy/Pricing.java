package org.example.ecommerce.strategy;

/**
 * Strategy pattern for order line totals: interchangeable pricing rules without
 * branching inside {@link org.example.ecommerce.OrderProcessingService}.
 */
public final class Pricing {

    private Pricing() {}

    /** Pricing algorithm contract. */
    public interface Strategy {

        double calculateTotal(double unitPrice, int quantity);

        /** Short label for confirmations / receipts. */
        String describe();
    }

    /** Delegates to the selected {@link Strategy}; callers swap strategies at runtime. */
    public static final class Context {

        private Strategy strategy;

        public Context(Strategy strategy) {
            this.strategy = strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public double calculateTotal(double unitPrice, int quantity) {
            return strategy.calculateTotal(unitPrice, quantity);
        }

        public String describe() {
            return strategy.describe();
        }
    }

    /** Full price: {@code unitPrice * quantity}. */
    public static final class Standard implements Strategy {

        @Override
        public double calculateTotal(double unitPrice, int quantity) {
            return unitPrice * quantity;
        }

        @Override
        public String describe() {
            return "Standard pricing — full price (no discount).";
        }
    }

    /** 10% off when quantity meets or exceeds the bulk threshold. */
    public static final class BulkDiscount implements Strategy {

        private static final int THRESHOLD = 10;
        private static final double DISCOUNT_RATE = 0.10;

        @Override
        public double calculateTotal(double unitPrice, int quantity) {
            double base = unitPrice * quantity;
            if (quantity >= THRESHOLD) {
                return base * (1 - DISCOUNT_RATE);
            }
            return base;
        }

        @Override
        public String describe() {
            return "Bulk pricing — 10% off when quantity is "
                    + THRESHOLD + " or more.";
        }
    }

    /** 15% loyalty / member discount on the line total. */
    public static final class Loyalty implements Strategy {

        private static final double DISCOUNT_RATE = 0.15;

        @Override
        public double calculateTotal(double unitPrice, int quantity) {
            return unitPrice * quantity * (1 - DISCOUNT_RATE);
        }

        @Override
        public String describe() {
            return "Loyalty pricing — 15% discount applied.";
        }
    }
}
