package org.example.controller;

import java.util.Scanner;

public class AdministrationController {

    private final Scanner scanner;

    public AdministrationController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Administration ---");
            System.out.println("1. Employee Records  (UC-FA1)");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> new EmployeeController(scanner).menu();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }
}
