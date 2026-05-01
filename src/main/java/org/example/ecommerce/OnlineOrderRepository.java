package org.example.ecommerce;

import org.example.model.OnlineOrder;
import org.example.util.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV persistence for {@link OnlineOrder} records.
 */
public class OnlineOrderRepository {

    public List<OnlineOrder> loadAll() {
        List<OnlineOrder> list = new ArrayList<>();
        for (String line : FileManager.readLines(ECommerceConstants.ORDERS_FILE)) {
            list.add(OnlineOrder.fromCSV(line));
        }
        return list;
    }

    public void append(OnlineOrder order) {
        FileManager.appendLine(ECommerceConstants.ORDERS_FILE, order.toCSV());
    }

    public void update(OnlineOrder updated) {
        List<String> lines = FileManager.readLines(ECommerceConstants.ORDERS_FILE);
        for (int i = 0; i < lines.size(); i++) {
            if (OnlineOrder.fromCSV(lines.get(i)).getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(ECommerceConstants.ORDERS_FILE, lines);
    }

    public OnlineOrder findById(int id) {
        for (String line : FileManager.readLines(ECommerceConstants.ORDERS_FILE)) {
            OnlineOrder o = OnlineOrder.fromCSV(line);
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }
}
