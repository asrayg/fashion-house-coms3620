package org.example.controller;

import java.util.Scanner;

public class FinanceController {

    private final Scanner scanner;

    public FinanceController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Finance ---");
            System.out.println("1. Payroll                    (UC-FA2)");
            System.out.println("2. Department Expenses        (UC-FA3)");
            System.out.println("3. Financial Summary Report   (UC-FA4)");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> new PayrollController(scanner).menu();
                case "2" -> new ExpenseController(scanner).menu();
                case "3" -> new FinancialReportController(scanner).menu();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }
}
