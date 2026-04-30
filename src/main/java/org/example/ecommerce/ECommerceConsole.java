package org.example.ecommerce;

import java.util.Scanner;

/**
 * Small read/validate helpers for the e-commerce console UI.
 */
final class ECommerceConsole {

    private ECommerceConsole() {}

    static int readInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return -1;
        }
    }

    static boolean validateEmail(String email) {
        return email.contains("@") && email.contains(".")
                && email.indexOf("@") < email.lastIndexOf(".");
    }
}
