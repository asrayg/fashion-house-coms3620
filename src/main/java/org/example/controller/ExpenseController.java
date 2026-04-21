package org.example.controller;

import org.example.model.Expense;
import org.example.util.FileManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * UC-FA3: Record Department Expense
 * Actor: Department Head
 */
public class ExpenseController {

    static final String FILE = "data/expenses.csv";

    private final Scanner scanner;

    public ExpenseController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Department Expenses ---");
            System.out.println("1. Record Expense");
            System.out.println("2. View Expense Report");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> recordExpense();
                case "2" -> viewExpenseReport();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC-FA3 — Record Expense
    // -------------------------------------------------------------------------

    private void recordExpense() {
        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        if (department.isEmpty()) { System.out.println("Error: Department cannot be blank."); return; }

        System.out.println("Category: 1) MATERIALS  2) LABOR  3) MARKETING  4) OVERHEAD  5) LEGAL  6) OTHER");
        System.out.print("Select: ");
        Expense.Category category;
        switch (scanner.nextLine().trim()) {
            case "1" -> category = Expense.Category.MATERIALS;
            case "2" -> category = Expense.Category.LABOR;
            case "3" -> category = Expense.Category.MARKETING;
            case "4" -> category = Expense.Category.OVERHEAD;
            case "5" -> category = Expense.Category.LEGAL;
            case "6" -> category = Expense.Category.OTHER;
            default  -> { System.out.println("Error: Invalid category."); return; }
        }

        System.out.print("Amount ($): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Amount must be a number.");
            return;
        }
        if (amount <= 0) { System.out.println("Error: Amount must be greater than 0."); return; }

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) { System.out.println("Error: Description cannot be blank."); return; }

        System.out.print("Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) { System.out.println("Error: Date cannot be blank."); return; }

        int id = FileManager.nextId(FILE);
        Expense expense = new Expense(id, department, category, amount, description, date);
        FileManager.appendLine(FILE, expense.toCSV());
        System.out.println("Expense recorded:");
        System.out.println(expense);

        // Show running department total
        double deptTotal = 0;
        for (String line : FileManager.readLines(FILE)) {
            Expense e = Expense.fromCSV(line);
            if (e.getDepartment().equalsIgnoreCase(department)) deptTotal += e.getAmount();
        }
        System.out.printf("Total recorded expenses for %s: $%.2f%n", department, deptTotal);
    }

    // -------------------------------------------------------------------------
    // View Expense Report
    // -------------------------------------------------------------------------

    private void viewExpenseReport() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) { System.out.println("No expenses on file."); return; }

        System.out.print("Filter by department (leave blank for all): ");
        String deptFilter = scanner.nextLine().trim().toLowerCase();

        System.out.print("Start date (YYYY-MM-DD, leave blank to skip): ");
        String startFilter = scanner.nextLine().trim();

        System.out.print("End date   (YYYY-MM-DD, leave blank to skip): ");
        String endFilter = scanner.nextLine().trim();

        Map<Expense.Category, Double> categoryTotals = new LinkedHashMap<>();
        for (Expense.Category c : Expense.Category.values()) categoryTotals.put(c, 0.0);

        System.out.println("\n--- Expense Report ---");
        double grandTotal = 0;
        int count = 0;
        for (String line : lines) {
            Expense e = Expense.fromCSV(line);
            if (!deptFilter.isEmpty() && !e.getDepartment().equalsIgnoreCase(deptFilter)) continue;
            if (!startFilter.isEmpty() && e.getDate().compareTo(startFilter) < 0) continue;
            if (!endFilter.isEmpty()   && e.getDate().compareTo(endFilter)   > 0) continue;

            System.out.println(e);
            categoryTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
            grandTotal += e.getAmount();
            count++;
        }

        if (count == 0) { System.out.println("No expenses match the given filters."); return; }

        System.out.println("\n--- Category Subtotals ---");
        for (Map.Entry<Expense.Category, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0)
                System.out.printf("  %-12s $%.2f%n", entry.getKey(), entry.getValue());
        }
        System.out.printf("Grand Total: $%.2f (%d expense(s))%n", grandTotal, count);
    }
}
