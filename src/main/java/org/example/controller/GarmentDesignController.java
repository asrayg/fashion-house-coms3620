package org.example.controller;

import org.example.model.Collection;
import org.example.model.GarmentDesign;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC2: Add Garment Design
 * Actor: Fashion Designer
 *
 * TODO (your name): Implement addGarmentDesign() and listGarments().
 */
public class GarmentDesignController {

    static final String FILE = "data/garments.csv";

    private final Scanner scanner;

    public GarmentDesignController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Garment Design ---");
            System.out.println("1. Add Garment Design");
            System.out.println("2. List All Garments");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> addGarmentDesign();
                case "2" -> listGarments();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Use Case 2 — Add Garment Design
    // -------------------------------------------------------------------------

    private void addGarmentDesign() {
        // TODO: Implement UC2 - Add Garment Design
        //
        // Steps:
        //   1. Prompt for collection ID
        //   2. Use CollectionController.findById(id) to verify it exists → error if null
        //   3. Prompt for: name, type, style, targetAudience, notes
        //   4. Validate none are blank
        //   5. Load existing garments; check for duplicate name in same collection → error if found
        //   6. Generate ID with FileManager.nextId(FILE)
        //   7. Build GarmentDesign object
        //   8. FileManager.appendLine(FILE, garment.toCSV())
        //   9. Print confirmation

        System.out.println("[TODO] addGarmentDesign() not yet implemented.");
    }

    // -------------------------------------------------------------------------
    // Helper — List Garments
    // -------------------------------------------------------------------------

    private void listGarments() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No garment designs on file.");
            return;
        }
        System.out.println("\n--- Garment Designs ---");
        for (String line : lines) {
            System.out.println(GarmentDesign.fromCSV(line));
        }
    }

    /**
     * Used by ProductSpecController to verify a garment exists.
     */
    public static GarmentDesign findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            GarmentDesign g = GarmentDesign.fromCSV(line);
            if (g.getId() == id) return g;
        }
        return null;
    }
}
