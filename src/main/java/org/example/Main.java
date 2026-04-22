package org.example;

import org.example.controller.*;

import java.util.Scanner;

/**
 * Fashion House Management System — Entry Point
 *
 * Console-based, file-backed. No GUI, no login required.
 * Run from the project root so relative paths (data/*.csv) resolve correctly.
 */
public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("============================================");
        System.out.println("   Fashion House Management System v2.0    ");
        System.out.println("============================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println();
            System.out.println("=== CORE (Iteration 1) ===");
            System.out.println("1.  Collection Management");
            System.out.println("2.  Garment Design");
            System.out.println("3.  Product Specifications");
            System.out.println("4.  Material Registry");
            System.out.println("5.  Material Orders");
            System.out.println();
            System.out.println("=== DEPARTMENTS (Iteration 2) ===");
            System.out.println("6.  Design Department           — Asray");
            System.out.println("7.  Production Department       — Maria");
            System.out.println("8.  Marketing & Campaigns       — Anoop");
            System.out.println("9.  Sales & Retail              — Billy");
            System.out.println("10. Finance & Administration    — Vinayak");
            System.out.println();
            System.out.println("=== STANDALONE USE CASES (Iteration 2) ===");
            System.out.println("11. Schedule Production Batch   — Asray");
            System.out.println("12. Assign Employee to Dept     — Maria");
            System.out.println("13. Record Material Usage       — Anoop");
            System.out.println("14. Update Production Status    — Billy");
            System.out.println("15. Receive Material Shipment   — Vinayak");
            System.out.println();
            System.out.println("0.  Exit");
            System.out.print("Select: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1"  -> new CollectionController(scanner).menu();
                case "2"  -> new GarmentDesignController(scanner).menu();
                case "3"  -> new ProductSpecificationController(scanner).menu();
                case "4"  -> new MaterialController(scanner).menu();
                case "5"  -> new MaterialOrderController(scanner).menu();
                case "6"  -> new DesignDepartmentController(scanner).menu();
                case "7"  -> new ProductionDepartmentController(scanner).menu();
                case "8"  -> new AdCampaignController(scanner).menu();
                case "9"  -> new SalesRetailController(scanner).menu();
                case "10" -> {
                    System.out.println("\n--- Finance & Administration ---");
                    System.out.println("1. Finance");
                    System.out.println("2. Administration");
                    System.out.print("Select: ");
                    String sub = scanner.nextLine().trim();
                    switch (sub) {
                        case "1" -> new FinanceController(scanner).menu();
                        case "2" -> new AdministrationController(scanner).menu();
                        default  -> System.out.println("Invalid option.");
                    }
                }
                case "11" -> new ProductionBatchController(scanner).menu();
                case "12" -> new DesignDepartmentController(scanner).employeeAssignmentMenu();
                case "13" -> new ProductionDepartmentController(scanner).menu();
                case "14" -> new ProductionDepartmentController(scanner).menu();
                case "15" -> new MaterialOrderController(scanner).menu();
                case "0"  -> running = false;
                default   -> System.out.println("Invalid option. Try again.");
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }
}
