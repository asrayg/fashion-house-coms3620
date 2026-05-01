package org.example.ecommerce;

import org.example.model.OnlineOrder;

/**
 * Virtual stock: {@link ECommerceConstants#BASE_STOCK_PER_SPEC} minus quantities on
 * non-cancelled, non-refunded orders.
 */
public class ECommerceInventoryService {

    private final OnlineOrderRepository orders;

    public ECommerceInventoryService(OnlineOrderRepository orders) {
        this.orders = orders;
    }

    public int getStockForSpec(int specId) {
        int ordered = orders.loadAll().stream()
                .filter(o -> o.getSpecId() == specId
                        && o.getStatus() != OnlineOrder.Status.CANCELLED
                        && o.getStatus() != OnlineOrder.Status.REFUNDED)
                .mapToInt(OnlineOrder::getQuantity)
                .sum();
        return Math.max(0, ECommerceConstants.BASE_STOCK_PER_SPEC - ordered);
    }

    /** New order row is appended after processing; stock is derived from orders. */
    public void deductStock(int specId, int quantity) {}

    /** Cancellation sets status to CANCELLED; stock is derived from orders. */
    public void restoreStock(int specId, int quantity) {}
}
