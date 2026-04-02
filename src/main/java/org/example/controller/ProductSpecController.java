package org.example.controller;

import org.example.model.GarmentDesign;
import org.example.model.ProductSpecification;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC3: Define Product Specification
 * Actor: Product Developer
 *
 * TODO (your name): Implement defineSpecification() and listSpecs().
 */
public class ProductSpecController {

    static final String FILE = "data/specifications.csv";

    private final Scanner scanner;

    public ProductSpecController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Product Specifications ---");
            System.out.println("1. Define Product Specification");
            System.out.println("2. List All Specifications");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> defineSpecification();
                case "2" -> listSpecs();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Use Case 3 — Define Product Specification
    // -------------------------------------------------------------------------

    private void defineSpecification() {
        // TODO: Implement UC3 - Define Product Specification
        //
        // Steps:
        //   1. Prompt for garment ID
        //   2. Use GarmentDesignController.findById(id) → error if null
        //   3. Prompt for: sizeRange, colorOptions, fabricType, measurements
        //   4. Validate sizeRange is not blank and colorOptions is not blank
        //   5. Generate ID with FileManager.nextId(FILE)
        //   6. Build ProductSpecification object
        //   7. FileManager.appendLine(FILE, spec.toCSV())
        //   8. Print confirmation

        System.out.println("[TODO] defineSpecification() not yet implemented.");
    }

    // -------------------------------------------------------------------------
    // Helper — List Specs
    // -------------------------------------------------------------------------

    private void listSpecs() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No specifications on file.");
            return;
        }
        System.out.println("\n--- Product Specifications ---");
        for (String line : lines) {
            System.out.println(ProductSpecification.fromCSV(line));
        }
    }

    /**
     * Used by iteration 2 (ProductionBatch) to verify a spec exists.
     */
    public static ProductSpecification findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            ProductSpecification s = ProductSpecification.fromCSV(line);
            if (s.getId() == id) return s;
        }
        return null;
    }
}
