package org.example.controller;

import org.example.model.ProductSpecification;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC3: Define Product Specification
 * Actor: Product Developer
 * Code Owner: Anoop Boyal
 */
public class ProductSpecificationController {

    static final String FILE = "data/design/specifications.csv";

    private final Scanner scanner;

    public ProductSpecificationController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Product Specification Management ---");
            System.out.println("1. Define Product Specification");
            System.out.println("2. List All Specifications");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> createSpecification();
                case "2" -> listSpecifications();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    private void createSpecification() {
        System.out.print("Garment Design ID: ");
        String designIdInput = scanner.nextLine().trim();

        int designId;
        try {
            designId = Integer.parseInt(designIdInput);
        } catch (NumberFormatException e) {
            System.out.println("Error: Garment design not found. Specification not created.");
            return;
        }

        if (GarmentDesignController.findById(designId) == null) {
            System.out.println("Error: Garment design not found. Specification not created.");
            return;
        }

        System.out.print("Size range (e.g. XS-XL): ");
        String sizeRange = scanner.nextLine().trim();

        System.out.print("Color options (e.g. Red, Blue, Black): ");
        String colorOptions = scanner.nextLine().trim();

        System.out.print("Fabric type (e.g. Cotton): ");
        String fabricType = scanner.nextLine().trim();

        System.out.print("Measurements (e.g. chest:40,waist:32): ");
        String measurements = scanner.nextLine().trim();

        if (sizeRange.isEmpty() || colorOptions.isEmpty()
                || fabricType.isEmpty() || measurements.isEmpty()) {
            System.out.println("Error: Invalid sizing data. Specification not created.");
            return;
        }

        if (!measurements.matches(".*\\d.*")) {
            System.out.println("Error: Invalid sizing data. Specification not created.");
            return;
        }

        int id = FileManager.nextId(FILE);
        ProductSpecification spec = new ProductSpecification(
            id, designId, sizeRange, colorOptions, fabricType, measurements
        );
        FileManager.appendLine(FILE, spec.toCSV());

        System.out.println("Specification created successfully: " + spec);
    }

    private void listSpecifications() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No specifications on file.");
            return;
        }
        System.out.println("\n--- Specifications ---");
        for (String line : lines) {
            System.out.println(ProductSpecification.fromCSV(line));
        }
    }

    /**
     * Used by other controllers to verify a specification exists.
     * @return the ProductSpecification or null if not found.
     */
    public static ProductSpecification findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            ProductSpecification s = ProductSpecification.fromCSV(line);
            if (s.getId() == id) return s;
        }
        return null;
    }
}
