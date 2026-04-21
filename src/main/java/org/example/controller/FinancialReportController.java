package org.example.controller;

import org.example.model.FinancialReport;
import org.example.model.Material;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * UC-FA4: Generate Financial Summary Report
 * Actor: Finance Manager
 *
 * Aggregates across:
 *   Revenue       — data/sales/sales.csv       (pipe-separated, total at index 6, date from timestamp index 1)
 *   Material Costs — data/material_orders.csv  (RECEIVED orders, qty * unitCost)
 *   Payroll Costs — data/payroll.csv           (netPay at index 8, payPeriodEnd at index 4)
 *   Other Expenses — data/expenses.csv         (amount at index 3, date at index 5)
 *   Marketing Spend — data/campaigns.csv       (totalBudget index 5, ACTIVE/COMPLETED, startDate index 6)
 */
public class FinancialReportController {

    static final String FILE = "data/financial_reports.csv";

    private static final String SALES_FILE    = "data/sales/sales.csv";
    private static final String ORDERS_FILE   = "data/material_orders.csv";
    private static final String PAYROLL_FILE  = PayrollController.FILE;
    private static final String EXPENSES_FILE = ExpenseController.FILE;
    private static final String CAMPAIGNS_FILE = "data/campaigns.csv";

    private final Scanner scanner;

    public FinancialReportController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Financial Reports ---");
            System.out.println("1. Generate Financial Summary Report");
            System.out.println("2. View Past Reports");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> generateReport();
                case "2" -> viewPastReports();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC-FA4 — Generate Financial Summary Report
    // -------------------------------------------------------------------------

    private void generateReport() {
        System.out.print("Report Period Start (YYYY-MM-DD): ");
        String startStr = scanner.nextLine().trim();
        System.out.print("Report Period End   (YYYY-MM-DD): ");
        String endStr = scanner.nextLine().trim();

        try {
            LocalDate.parse(startStr);
            LocalDate.parse(endStr);
        } catch (Exception e) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (endStr.compareTo(startStr) < 0) {
            System.out.println("Error: End date must be on or after start date.");
            return;
        }

        // --- Revenue: pipe-separated sales, total at index 6, date from timestamp index 1 ---
        double totalRevenue = 0;
        int saleCount = 0;
        for (String line : FileManager.readLines(SALES_FILE)) {
            String[] p = line.split("\\|", -1);
            if (p.length < 7) continue;
            String saleDate = p[1].trim().length() >= 10 ? p[1].trim().substring(0, 10) : "";
            if (inRange(saleDate, startStr, endStr)) {
                try { totalRevenue += Double.parseDouble(p[6].trim()); saleCount++; }
                catch (NumberFormatException ignored) {}
            }
        }

        // --- Material Costs: RECEIVED orders, qty * material unitCost, date = expectedDelivery ---
        double materialCosts = 0;
        for (String line : FileManager.readLines(ORDERS_FILE)) {
            String[] p = line.split(",", 6);
            if (p.length < 6) continue;
            if (!p[5].trim().equals("RECEIVED")) continue;
            String deliveryDate = p[4].trim();
            if (inRange(deliveryDate, startStr, endStr)) {
                int matId = Integer.parseInt(p[1].trim());
                int qty   = Integer.parseInt(p[3].trim());
                Material mat = MaterialController.findById(matId);
                if (mat != null) materialCosts += qty * mat.getUnitCost();
            }
        }

        // --- Payroll Costs: netPay at index 8, payPeriodEnd at index 4 ---
        double payrollCosts = 0;
        for (String line : FileManager.readLines(PAYROLL_FILE)) {
            String[] p = line.split(",", 10);
            if (p.length < 10) continue;
            String periodEnd = p[4].trim();
            if (inRange(periodEnd, startStr, endStr)) {
                try { payrollCosts += Double.parseDouble(p[8].trim()); }
                catch (NumberFormatException ignored) {}
            }
        }

        // --- Other Expenses: amount at index 3, date at index 5 ---
        double otherExpenses = 0;
        for (String line : FileManager.readLines(EXPENSES_FILE)) {
            String[] p = line.split(",", 6);
            if (p.length < 6) continue;
            String expDate = p[5].trim();
            if (inRange(expDate, startStr, endStr)) {
                try { otherExpenses += Double.parseDouble(p[3].trim()); }
                catch (NumberFormatException ignored) {}
            }
        }

        // --- Marketing Spend: totalBudget at index 5, ACTIVE/COMPLETED, startDate at index 6 ---
        double marketingSpend = 0;
        for (String line : FileManager.readLines(CAMPAIGNS_FILE)) {
            String[] p = line.split(",", -1);
            if (p.length < 9) continue;
            String status = p[8].trim();
            if (!status.equals("ACTIVE") && !status.equals("COMPLETED")) continue;
            String campStart = p[6].trim();
            if (inRange(campStart, startStr, endStr)) {
                try { marketingSpend += Double.parseDouble(p[5].trim()); }
                catch (NumberFormatException ignored) {}
            }
        }

        // --- Compute P&L ---
        double grossProfit = totalRevenue - materialCosts;
        double netProfit   = grossProfit - payrollCosts - otherExpenses - marketingSpend;

        // --- Display ---
        System.out.println("\n========================================");
        System.out.println("       FINANCIAL SUMMARY REPORT         ");
        System.out.println("========================================");
        System.out.printf("Period        : %s to %s%n", startStr, endStr);
        System.out.printf("Generated     : %s%n", LocalDate.now());
        System.out.println("----------------------------------------");
        System.out.printf("Revenue       : $%,.2f  (%d sale(s))%n", totalRevenue, saleCount);
        if (saleCount == 0) System.out.println("  (No sales recorded in this period)");
        System.out.println("----------------------------------------");
        System.out.printf("Material Costs: $%,.2f%n", materialCosts);
        System.out.printf("Gross Profit  : $%,.2f%n", grossProfit);
        System.out.println("----------------------------------------");
        System.out.printf("Payroll       : $%,.2f%n", payrollCosts);
        System.out.printf("Expenses      : $%,.2f%n", otherExpenses);
        System.out.printf("Marketing     : $%,.2f%n", marketingSpend);
        System.out.println("----------------------------------------");
        System.out.printf("Net Profit    : $%,.2f%n", netProfit);
        System.out.println("========================================");

        // --- Persist ---
        int id = FileManager.nextId(FILE);
        FinancialReport report = new FinancialReport(
                id, startStr, endStr,
                totalRevenue, materialCosts, payrollCosts,
                otherExpenses, marketingSpend,
                grossProfit, netProfit,
                LocalDate.now().toString()
        );
        FileManager.appendLine(FILE, report.toCSV());
        System.out.println("Report saved (ID: " + id + ").");
    }

    // -------------------------------------------------------------------------
    // View Past Reports
    // -------------------------------------------------------------------------

    private void viewPastReports() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) { System.out.println("No financial reports on file."); return; }
        System.out.println("\n--- Past Financial Reports ---");
        for (String line : lines) {
            System.out.println(FinancialReport.fromCSV(line));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private boolean inRange(String date, String start, String end) {
        if (date == null || date.isEmpty()) return false;
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }
}
