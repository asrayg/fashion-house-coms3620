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
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }
}
