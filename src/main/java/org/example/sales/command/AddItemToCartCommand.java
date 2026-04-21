package org.example.sales.command;

import org.example.sales.model.RetailItem;
import org.example.sales.model.SaleLine;

import java.util.List;

/**
 * Adds a selected retail item to the current cart (merges quantity if already present).
 */
public class AddItemToCartCommand implements SalesCommand {

    private final List<SaleLine> cart;
    private final RetailItem item;
    private final int quantity;

    public AddItemToCartCommand(List<SaleLine> cart, RetailItem item, int quantity) {
        this.cart = cart;
        this.item = item;
        this.quantity = quantity;
    }

    @Override
    public void execute() {
        for (SaleLine line : cart) {
            if (line.getItemId() == item.getId()) {
                line.setQuantity(line.getQuantity() + quantity);
                return;
            }
        }
        cart.add(new SaleLine(item.getId(), item.getSku(), item.getName(), item.getUnitPrice(), quantity));
    }
}
