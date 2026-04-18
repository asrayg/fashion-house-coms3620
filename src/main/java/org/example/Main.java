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
            System.out.println("1. Collection Management       (UC1)");
            System.out.println("2. Garment Design              (UC2)");
            System.out.println("3. Product Specifications      (UC3)");
            System.out.println("4. Material Registry           (UC4)");
            System.out.println("5. Material Orders             (UC5)");
            System.out.println("6. Production Batches          (UC6 - Iter2)");
            System.out.println("0. Exit");
            System.out.print("Select: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> new CollectionController(scanner).menu();
                case "2" -> new GarmentDesignController(scanner).menu();
                case "3" -> new ProductSpecController(scanner).menu();
                case "4" -> new MaterialController(scanner).menu();
                case "5" -> new MaterialOrderController(scanner).menu();
                case "6" -> new ProductionBatchController(scanner).menu();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option. Try again.");
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }
}
