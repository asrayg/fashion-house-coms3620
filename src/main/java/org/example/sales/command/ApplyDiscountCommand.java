package org.example.sales.command;

/**
 * Applies discount rules to a subtotal.
 */
public class ApplyDiscountCommand implements SalesCommand {

    private final double subtotal;
    private final String discountType;
    private final double discountValue;
    private double computedDiscountAmount;
    private double computedFinalTotal;

    public ApplyDiscountCommand(double subtotal, String discountType, double discountValue) {
        this.subtotal = subtotal;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.computedDiscountAmount = 0.0;
        this.computedFinalTotal = subtotal;
    }

    @Override
    public void execute() {
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            computedDiscountAmount = subtotal * (discountValue / 100.0);
        } else if ("FIXED".equalsIgnoreCase(discountType)) {
            computedDiscountAmount = discountValue;
        } else {
            computedDiscountAmount = 0.0;
        }

        if (computedDiscountAmount < 0) computedDiscountAmount = 0.0;
        if (computedDiscountAmount > subtotal) computedDiscountAmount = subtotal;
        computedFinalTotal = subtotal - computedDiscountAmount;
    }

    public double getComputedDiscountAmount() {
        return computedDiscountAmount;
    }

    public double getComputedFinalTotal() {
        return computedFinalTotal;
    }
}
