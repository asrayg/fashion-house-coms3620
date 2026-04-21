package org.example.sales;

import org.example.sales.command.AddItemToCartCommand;
import org.example.sales.command.ApplyDiscountCommand;
import org.example.sales.command.SalesCommandInvoker;
import org.example.sales.model.RetailItem;
import org.example.sales.model.ReturnRecord;
import org.example.sales.model.SaleLine;
import org.example.sales.model.SaleRecord;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SalesRetailController {

    private static final String ITEMS_FILE = "data/sales/retail_items.csv";
    private static final String SALES_FILE = "data/sales/sales.csv";
    private static final String RETURNS_FILE = "data/sales/returns.csv";
    private static final int LOW_STOCK_THRESHOLD = 3;
    private static final int RETURN_WINDOW_DAYS = 30;

    private final Scanner scanner;

    public SalesRetailController(Scanner scanner) {
        this.scanner = scanner;
        seedInventoryIfEmpty();
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Sales and Retail ---");
            System.out.println("1. Process Sale");
            System.out.println("2. Apply Discount (Estimate)");
            System.out.println("3. Process Return");
            System.out.println("4. Check Inventory");
            System.out.println("5. Generate Sales Report");
            System.out.println("6. List Sales");
            System.out.println("7. Reset Sales Demo Data");
            System.out.println("0. Back");
            System.out.println("Hint: Type only the menu number (for example, 1 or 5).\n");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> processSale();
                case "2" -> applyDiscountEstimate();
                case "3" -> processReturn();
                case "4" -> checkInventory();
                case "5" -> generateSalesReport();
                case "6" -> listSales();
                case "7" -> resetSalesData();
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void processSale() {
        List<RetailItem> inventory = loadInventory();
        if (inventory.isEmpty()) {
            System.out.println("No inventory available.");
            return;
        }

        List<SaleLine> cart = new ArrayList<>();
        SalesCommandInvoker invoker = new SalesCommandInvoker();

        System.out.println("\nSale input guide:");
        System.out.println("- Type SKU exactly (example: DRS-1001)");
        System.out.println("- Type LIST to view all items");
        System.out.println("- Type DONE to finalize cart");
        System.out.println("- Type CANCEL to abort sale");
        while (true) {
            System.out.print("SKU (example DRS-1001 | LIST | DONE | CANCEL): ");
            String skuInput = scanner.nextLine().trim();
            if (skuInput.equalsIgnoreCase("CANCEL")) {
                System.out.println("Sale canceled.");
                return;
            }
            if (skuInput.equalsIgnoreCase("LIST")) {
                printInventory(inventory, "", "");
                continue;
            }
            if (skuInput.equalsIgnoreCase("DONE")) {
                if (cart.isEmpty()) {
                    System.out.println("Add at least one item before finalizing.");
                    continue;
                }
                break;
            }

            RetailItem item = findItemBySku(inventory, skuInput);
            if (item == null) {
                System.out.println("Item not found. Use LIST to see available SKUs.");
                continue;
            }

            System.out.print("Quantity (whole number, example 1): ");
            int qty;
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Quantity must be a whole number (example: 1, 2, 3).");
                continue;
            }

            if (qty <= 0) {
                System.out.println("Quantity must be greater than 0.");
                continue;
            }

            int existingQty = quantityAlreadyInCart(cart, item.getId());
            if (existingQty + qty > item.getStockQuantity()) {
                System.out.println("Insufficient stock. Available: " + (item.getStockQuantity() - existingQty));
                continue;
            }

            invoker.run(new AddItemToCartCommand(cart, item, qty), "Add " + qty + " x " + item.getSku());
            System.out.println("Added: " + item.getName() + " x " + qty);
            printCart(cart);
        }

        double subtotal = computeSubtotal(cart);
        String discountType = "NONE";
        double discountValue = 0.0;
        double discountAmount = 0.0;
        double total = subtotal;

        System.out.print("Apply discount? (y/n, example y): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            DiscountDecision decision = captureDiscount(subtotal, invoker);
            if (decision != null) {
                discountType = decision.type;
                discountValue = decision.value;
                discountAmount = decision.amount;
                total = decision.finalTotal;
            }
        }

        System.out.print("Payment method (Cash/Card/Transfer, example Card): ");
        String paymentMethod = scanner.nextLine().trim();
        if (paymentMethod.isEmpty()) paymentMethod = "Unknown";

        int saleId = FileManager.nextId(SALES_FILE);
        SaleRecord sale = new SaleRecord(
            saleId,
            LocalDateTime.now(),
            paymentMethod,
            discountType,
            discountValue,
            subtotal,
            total,
            "COMPLETED",
            copyLines(cart)
        );

        FileManager.appendLine(SALES_FILE, sale.toStorageLine());
        decrementInventory(inventory, cart);
        saveInventory(inventory);

        System.out.println("\n--- Receipt ---");
        System.out.println("Sale ID: " + saleId);
        printCart(cart);
        System.out.println("Subtotal: $" + formatMoney(subtotal));
        System.out.println("Discount: $" + formatMoney(discountAmount));
        System.out.println("Total:    $" + formatMoney(total));
        System.out.println("Payment: " + paymentMethod);
        System.out.println("Sale completed.");
        printLowStockAlerts(inventory);
        pauseForFiveSeconds();
    }

    private void applyDiscountEstimate() {
        System.out.print("Subtotal amount (number, example 120.50): ");
        double subtotal;
        try {
            subtotal = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Subtotal must be numeric (example: 120.50).");
            return;
        }

        if (subtotal <= 0) {
            System.out.println("Subtotal must be greater than zero.");
            return;
        }

        SalesCommandInvoker invoker = new SalesCommandInvoker();
        DiscountDecision decision = captureDiscount(subtotal, invoker);
        if (decision == null) return;

        System.out.println("Discount type: " + decision.type);
        System.out.println("Discount amount: $" + formatMoney(decision.amount));
        System.out.println("Final total:      $" + formatMoney(decision.finalTotal));
    }

    private void processReturn() {
        List<SaleRecord> sales = loadSales();
        if (sales.isEmpty()) {
            System.out.println("No sales available for return.");
            return;
        }

        System.out.print("Sale ID to return from (whole number, example 1): ");
        int saleId;
        try {
            saleId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Sale ID must be numeric (example: 1).");
            return;
        }

        SaleRecord sale = null;
        for (SaleRecord current : sales) {
            if (current.getId() == saleId) {
                sale = current;
                break;
            }
        }

        if (sale == null) {
            System.out.println("Sale not found.");
            return;
        }

        if (sale.getTimestamp().isBefore(LocalDateTime.now().minusDays(RETURN_WINDOW_DAYS))) {
            System.out.println("Return rejected: sale is older than " + RETURN_WINDOW_DAYS + " days.");
            return;
        }

        List<ReturnRecord> returns = loadReturns();

        System.out.println("Select item to return:");
        for (int i = 0; i < sale.getLines().size(); i++) {
            SaleLine line = sale.getLines().get(i);
            int alreadyReturned = returnedQtyForSaleItem(returns, sale.getId(), line.getItemId());
            int remaining = line.getQuantity() - alreadyReturned;
            System.out.println((i + 1) + ". " + line.getSku() + " - " + line.getItemName() +
                " | Sold: " + line.getQuantity() + " | Returnable: " + remaining);
        }

        System.out.print("Line number (choose from list above, example 1): ");
        int lineNumber;
        try {
            lineNumber = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Line number must be numeric (example: 1).");
            return;
        }

        if (lineNumber < 1 || lineNumber > sale.getLines().size()) {
            System.out.println("Invalid line number.");
            return;
        }

        SaleLine line = sale.getLines().get(lineNumber - 1);
        int alreadyReturned = returnedQtyForSaleItem(returns, sale.getId(), line.getItemId());
        int maxReturnable = line.getQuantity() - alreadyReturned;
        if (maxReturnable <= 0) {
            System.out.println("This item has already been fully returned.");
            return;
        }

        System.out.print("Return quantity (whole number, max " + maxReturnable + ", example 1): ");
        int returnQty;
        try {
            returnQty = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Quantity must be numeric (whole number, example: 1).");
            return;
        }

        if (returnQty <= 0 || returnQty > maxReturnable) {
            System.out.println("Invalid return quantity.");
            return;
        }

        double refundAmount = line.getUnitPrice() * returnQty;
        ReturnRecord record = new ReturnRecord(
            FileManager.nextId(RETURNS_FILE),
            sale.getId(),
            line.getItemId(),
            line.getSku(),
            returnQty,
            refundAmount,
            LocalDateTime.now()
        );
        FileManager.appendLine(RETURNS_FILE, record.toStorageLine());

        List<RetailItem> inventory = loadInventory();
        for (RetailItem item : inventory) {
            if (item.getId() == line.getItemId()) {
                item.setStockQuantity(item.getStockQuantity() + returnQty);
                break;
            }
        }
        saveInventory(inventory);

        updateSaleStatusBasedOnReturns(sale, returns, record, sales);

        System.out.println("Return processed. Refund amount: $" + formatMoney(refundAmount));
        pauseForFiveSeconds();
    }

    private void checkInventory() {
        List<RetailItem> inventory = loadInventory();
        if (inventory.isEmpty()) {
            System.out.println("No inventory records.");
            return;
        }

        System.out.print("Search SKU (optional, example DRS-1001, Enter to skip): ");
        String sku = scanner.nextLine().trim();
        System.out.print("Search Name (optional, example Dress, Enter to skip): ");
        String name = scanner.nextLine().trim();
        System.out.print("Search Category (optional, example Dresses, Enter to skip): ");
        String category = scanner.nextLine().trim();

        printInventory(inventory, sku, (name + " " + category).trim());
    }

    private void generateSalesReport() {
        LocalDate start;
        LocalDate end;
        try {
            System.out.print("Start date (YYYY-MM-DD, example 2026-04-01): ");
            start = LocalDate.parse(scanner.nextLine().trim());
            System.out.print("End date (YYYY-MM-DD, example 2026-04-30): ");
            end = LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD (example: 2026-04-30).");
            return;
        }

        if (end.isBefore(start)) {
            System.out.println("End date cannot be before start date.");
            return;
        }

        List<SaleRecord> sales = loadSales();
        List<ReturnRecord> returns = loadReturns();
        int transactionCount = 0;
        double grossRevenue = 0.0;
        Map<String, Integer> itemQty = new HashMap<>();
        Map<String, Integer> categoryQty = new HashMap<>();
        Map<String, String> skuToCategory = new HashMap<>();

        List<RetailItem> inventory = loadInventory();
        for (RetailItem item : inventory) {
            skuToCategory.put(item.getSku(), item.getCategory());
        }

        for (SaleRecord sale : sales) {
            LocalDate date = sale.getTimestamp().toLocalDate();
            if (date.isBefore(start) || date.isAfter(end)) continue;

            transactionCount++;
            grossRevenue += sale.getTotal();
            for (SaleLine line : sale.getLines()) {
                itemQty.put(line.getSku(), itemQty.getOrDefault(line.getSku(), 0) + line.getQuantity());
                String category = skuToCategory.getOrDefault(line.getSku(), "Unknown");
                categoryQty.put(category, categoryQty.getOrDefault(category, 0) + line.getQuantity());
            }
        }

        double refundTotal = 0.0;
        for (ReturnRecord rr : returns) {
            LocalDate date = rr.getTimestamp().toLocalDate();
            if (date.isBefore(start) || date.isAfter(end)) continue;
            refundTotal += rr.getRefundAmount();
        }

        String topSku = "N/A";
        int topQty = 0;
        for (Map.Entry<String, Integer> entry : itemQty.entrySet()) {
            if (entry.getValue() > topQty) {
                topQty = entry.getValue();
                topSku = entry.getKey();
            }
        }

        System.out.println("\n--- Sales Report ---");
        System.out.println("Range: " + start + " to " + end);
        System.out.println("Transactions: " + transactionCount);
        System.out.println("Gross Revenue: $" + formatMoney(grossRevenue));
        System.out.println("Refunds:       $" + formatMoney(refundTotal));
        System.out.println("Net Revenue:   $" + formatMoney(grossRevenue - refundTotal));
        System.out.println("Top Seller SKU: " + topSku + " (" + topQty + " units)");

        System.out.println("\nTop 3 SKUs by units sold:");
        printTopSkus(itemQty, 3);

        System.out.println("\nUnits sold by category:");
        if (categoryQty.isEmpty()) {
            System.out.println("No category data for selected range.");
        } else {
            for (Map.Entry<String, Integer> entry : categoryQty.entrySet()) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue());
            }
        }

        pauseForFiveSeconds();
    }

    private void listSales() {
        List<SaleRecord> sales = loadSales();
        if (sales.isEmpty()) {
            System.out.println("No sales records yet.");
            return;
        }

        System.out.println("\n--- Sales ---");
        for (SaleRecord sale : sales) {
            System.out.println("Sale " + sale.getId() + " | " + sale.getTimestamp() +
                " | Total: $" + formatMoney(sale.getTotal()) +
                " | Status: " + sale.getStatus() +
                " | Payment: " + sale.getPaymentMethod());
        }
    }

    private DiscountDecision captureDiscount(double subtotal, SalesCommandInvoker invoker) {
        System.out.print("Discount type (PERCENT/FIXED/NONE, example PERCENT): ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (type.equals("NONE") || type.isEmpty()) {
            return new DiscountDecision("NONE", 0.0, 0.0, subtotal);
        }
        if (!type.equals("PERCENT") && !type.equals("FIXED")) {
            System.out.println("Invalid discount type. Use PERCENT, FIXED, or NONE.");
            return null;
        }

        if (type.equals("PERCENT")) {
            System.out.print("Discount value (0-100, example 10 for 10%): ");
        } else {
            System.out.print("Discount value (currency amount, example 15.50): ");
        }
        double value;
        try {
            value = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Discount value must be numeric (examples: 10 or 15.50).");
            return null;
        }

        if (value < 0) {
            System.out.println("Discount value cannot be negative.");
            return null;
        }

        if (type.equals("PERCENT") && value > 100) {
            System.out.println("Percentage discount cannot exceed 100 (example valid value: 10).");
            return null;
        }

        ApplyDiscountCommand cmd = new ApplyDiscountCommand(subtotal, type, value);
        invoker.run(cmd, "Apply " + type + " discount");
        return new DiscountDecision(type, value, cmd.getComputedDiscountAmount(), cmd.getComputedFinalTotal());
    }

    private void printInventory(List<RetailItem> items, String skuFilter, String freeTextFilter) {
        String skuKey = skuFilter == null ? "" : skuFilter.trim().toLowerCase();
        String textKey = freeTextFilter == null ? "" : freeTextFilter.trim().toLowerCase();
        boolean any = false;

        System.out.println("\n--- Inventory ---");
        for (RetailItem item : items) {
            boolean skuMatch = skuKey.isEmpty() || item.getSku().toLowerCase().contains(skuKey);
            boolean textMatch = textKey.isEmpty() ||
                item.getName().toLowerCase().contains(textKey) ||
                item.getCategory().toLowerCase().contains(textKey);
            if (skuMatch && textMatch) {
                any = true;
                String lowStockLabel = item.getStockQuantity() <= LOW_STOCK_THRESHOLD ? " [LOW STOCK]" : "";
                System.out.println(item + lowStockLabel);
            }
        }

        if (!any) {
            System.out.println("No items match the search criteria.");
        }
    }

    private void printCart(List<SaleLine> cart) {
        System.out.println("\nCurrent cart:");
        for (SaleLine line : cart) {
            System.out.println("- " + line.getSku() + " | " + line.getItemName() +
                " | Qty: " + line.getQuantity() +
                " | Unit: $" + formatMoney(line.getUnitPrice()) +
                " | Line Total: $" + formatMoney(line.getLineTotal()));
        }
        System.out.println("Cart Subtotal: $" + formatMoney(computeSubtotal(cart)));
    }

    private double computeSubtotal(List<SaleLine> cart) {
        double subtotal = 0.0;
        for (SaleLine line : cart) subtotal += line.getLineTotal();
        return subtotal;
    }

    private int quantityAlreadyInCart(List<SaleLine> cart, int itemId) {
        for (SaleLine line : cart) {
            if (line.getItemId() == itemId) return line.getQuantity();
        }
        return 0;
    }

    private RetailItem findItemBySku(List<RetailItem> items, String sku) {
        for (RetailItem item : items) {
            if (item.getSku().equalsIgnoreCase(sku)) return item;
        }
        return null;
    }

    private List<SaleLine> copyLines(List<SaleLine> lines) {
        List<SaleLine> copy = new ArrayList<>();
        for (SaleLine line : lines) {
            copy.add(new SaleLine(
                line.getItemId(),
                line.getSku(),
                line.getItemName(),
                line.getUnitPrice(),
                line.getQuantity()
            ));
        }
        return copy;
    }

    private void decrementInventory(List<RetailItem> inventory, List<SaleLine> lines) {
        for (SaleLine line : lines) {
            for (RetailItem item : inventory) {
                if (item.getId() == line.getItemId()) {
                    item.setStockQuantity(item.getStockQuantity() - line.getQuantity());
                    break;
                }
            }
        }
    }

    private List<RetailItem> loadInventory() {
        List<RetailItem> items = new ArrayList<>();
        for (String line : FileManager.readLines(ITEMS_FILE)) {
            items.add(RetailItem.fromCSV(line));
        }
        return items;
    }

    private void saveInventory(List<RetailItem> items) {
        List<String> lines = new ArrayList<>();
        for (RetailItem item : items) lines.add(item.toCSV());
        FileManager.writeLines(ITEMS_FILE, lines);
    }

    private List<SaleRecord> loadSales() {
        List<SaleRecord> sales = new ArrayList<>();
        for (String line : FileManager.readLines(SALES_FILE)) {
            sales.add(SaleRecord.fromStorageLine(line));
        }
        return sales;
    }

    private List<ReturnRecord> loadReturns() {
        List<ReturnRecord> returns = new ArrayList<>();
        for (String line : FileManager.readLines(RETURNS_FILE)) {
            returns.add(ReturnRecord.fromStorageLine(line));
        }
        return returns;
    }

    private int returnedQtyForSaleItem(List<ReturnRecord> returns, int saleId, int itemId) {
        int qty = 0;
        for (ReturnRecord record : returns) {
            if (record.getSaleId() == saleId && record.getItemId() == itemId) {
                qty += record.getQuantity();
            }
        }
        return qty;
    }

    private void updateSaleStatusBasedOnReturns(SaleRecord sale, List<ReturnRecord> existingReturns,
                                                ReturnRecord newReturn, List<SaleRecord> allSales) {
        int soldTotalQty = 0;
        for (SaleLine line : sale.getLines()) soldTotalQty += line.getQuantity();

        int returnedTotalQty = newReturn.getQuantity();
        for (ReturnRecord record : existingReturns) {
            if (record.getSaleId() == sale.getId()) returnedTotalQty += record.getQuantity();
        }

        if (returnedTotalQty >= soldTotalQty) {
            sale.setStatus("RETURNED");
        } else if (returnedTotalQty > 0) {
            sale.setStatus("PARTIALLY_RETURNED");
        }

        List<String> lines = new ArrayList<>();
        for (SaleRecord record : allSales) {
            if (record.getId() == sale.getId()) {
                lines.add(sale.toStorageLine());
            } else {
                lines.add(record.toStorageLine());
            }
        }
        FileManager.writeLines(SALES_FILE, lines);
    }

    private void seedInventoryIfEmpty() {
        if (FileManager.hasRecords(ITEMS_FILE)) return;

        List<String> defaults = new ArrayList<>();
        defaults.add(new RetailItem(1, "DRS-1001", "Midnight Silk Dress", "Dresses", 149.99, 8).toCSV());
        defaults.add(new RetailItem(2, "TOP-2001", "Linen Structured Blouse", "Tops", 69.50, 15).toCSV());
        defaults.add(new RetailItem(3, "PNT-3001", "Tailored Wide-Leg Trousers", "Bottoms", 89.00, 12).toCSV());
        defaults.add(new RetailItem(4, "ACC-4001", "Leather Belt", "Accessories", 39.99, 20).toCSV());
        defaults.add(new RetailItem(5, "BAG-5001", "Mini Crossbody Bag", "Accessories", 110.00, 9).toCSV());
        defaults.add(new RetailItem(6, "FTW-6001", "City Leather Loafers", "Footwear", 129.95, 10).toCSV());
        defaults.add(new RetailItem(7, "OUT-7001", "Wool Blend Blazer", "Outerwear", 199.00, 7).toCSV());
        defaults.add(new RetailItem(8, "DRS-1002", "Pleated Evening Gown", "Dresses", 249.00, 5).toCSV());
        FileManager.writeLines(ITEMS_FILE, defaults);
    }

    private void resetSalesData() {
        System.out.print("This will clear sales/returns and reset inventory. Type RESET to confirm: ");
        String confirm = scanner.nextLine().trim();
        if (!"RESET".equalsIgnoreCase(confirm)) {
            System.out.println("Reset canceled.");
            return;
        }

        FileManager.writeLines(SALES_FILE, new ArrayList<>());
        FileManager.writeLines(RETURNS_FILE, new ArrayList<>());
        FileManager.writeLines(ITEMS_FILE, defaultInventoryLines());
        System.out.println("Sales demo data reset complete.");
        pauseForFiveSeconds();
    }

    private List<String> defaultInventoryLines() {
        List<String> defaults = new ArrayList<>();
        defaults.add(new RetailItem(1, "DRS-1001", "Midnight Silk Dress", "Dresses", 149.99, 8).toCSV());
        defaults.add(new RetailItem(2, "TOP-2001", "Linen Structured Blouse", "Tops", 69.50, 15).toCSV());
        defaults.add(new RetailItem(3, "PNT-3001", "Tailored Wide-Leg Trousers", "Bottoms", 89.00, 12).toCSV());
        defaults.add(new RetailItem(4, "ACC-4001", "Leather Belt", "Accessories", 39.99, 20).toCSV());
        defaults.add(new RetailItem(5, "BAG-5001", "Mini Crossbody Bag", "Accessories", 110.00, 9).toCSV());
        defaults.add(new RetailItem(6, "FTW-6001", "City Leather Loafers", "Footwear", 129.95, 10).toCSV());
        defaults.add(new RetailItem(7, "OUT-7001", "Wool Blend Blazer", "Outerwear", 199.00, 7).toCSV());
        defaults.add(new RetailItem(8, "DRS-1002", "Pleated Evening Gown", "Dresses", 249.00, 5).toCSV());
        return defaults;
    }

    private void printLowStockAlerts(List<RetailItem> inventory) {
        List<RetailItem> lowStockItems = new ArrayList<>();
        for (RetailItem item : inventory) {
            if (item.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                lowStockItems.add(item);
            }
        }

        if (lowStockItems.isEmpty()) return;

        System.out.println("\nLow stock alerts (threshold <= " + LOW_STOCK_THRESHOLD + "):");
        for (RetailItem low : lowStockItems) {
            System.out.println("- " + low.getSku() + " | " + low.getName() + " | Stock: " + low.getStockQuantity());
        }
    }

    private void printTopSkus(Map<String, Integer> itemQty, int limit) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(itemQty.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        if (entries.isEmpty()) {
            System.out.println("No SKU sales in selected range.");
            return;
        }

        int max = Math.min(limit, entries.size());
        for (int i = 0; i < max; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            System.out.println((i + 1) + ". " + entry.getKey() + " - " + entry.getValue() + " units");
        }
    }

    private String formatMoney(double value) {
        return String.format("%.2f", value);
    }

    private void pauseForFiveSeconds() {
        try {
            System.out.println("Pausing for 5 seconds so you can read the result...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class DiscountDecision {
        private final String type;
        private final double value;
        private final double amount;
        private final double finalTotal;

        private DiscountDecision(String type, double value, double amount, double finalTotal) {
            this.type = type;
            this.value = value;
            this.amount = amount;
            this.finalTotal = finalTotal;
        }
    }
}
