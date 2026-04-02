package org.example.controller;

import org.example.model.Collection;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC1: Create Collection
 * Actor: Design Manager
 *
 * TODO (your name): Implement createCollection() and listCollections().
 */
public class CollectionController {

    static final String FILE = "data/collections.csv";

    private final Scanner scanner;

    public CollectionController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Collection Management ---");
            System.out.println("1. Create Collection");
            System.out.println("2. List All Collections");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> createCollection();
                case "2" -> listCollections();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Use Case 1 — Create Collection
    // -------------------------------------------------------------------------

    private void createCollection() {
        // TODO: Implement UC1 - Create Collection
        //
        // Steps:
        //   1. Prompt for: name, season, releasePeriod, description
        //   2. Validate none are blank
        //   3. Load existing collections via FileManager.readLines(FILE)
        //   4. Check for duplicate (same name + season) → print error and return if found
        //   5. Generate ID with FileManager.nextId(FILE)
        //   6. Build new Collection object
        //   7. FileManager.appendLine(FILE, collection.toCSV())
        //   8. Print confirmation

        System.out.println("[TODO] createCollection() not yet implemented.");
    }

    // -------------------------------------------------------------------------
    // Helper — List Collections (useful for other controllers too)
    // -------------------------------------------------------------------------

    private void listCollections() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No collections on file.");
            return;
        }
        System.out.println("\n--- Collections ---");
        for (String line : lines) {
            System.out.println(Collection.fromCSV(line));
        }
    }

    /**
     * Used by GarmentDesignController to verify a collection exists.
     * @return the Collection or null if not found.
     */
    public static Collection findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            Collection c = Collection.fromCSV(line);
            if (c.getId() == id) return c;
        }
        return null;
    }
}
