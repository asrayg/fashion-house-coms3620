package org.example.controller;

import org.example.model.Collection;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC1: Create Collection
 * Actor: Design Manager
 *
 * TODO Asray Gopa: Implement createCollection() and listCollections().
 */
public class CollectionController {

    static final String FILE = "data/design/collections.csv";

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
        System.out.print("Collection name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Season (e.g. Summer 2025): ");
        String season = scanner.nextLine().trim();

        System.out.print("Release period (e.g. Jan-Mar 2025): ");
        String releasePeriod = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        if (name.isEmpty() || season.isEmpty() || releasePeriod.isEmpty() || description.isEmpty()) {
            System.out.println("Error: All fields are required. Collection not created.");
            return;
        }

        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            Collection existing = Collection.fromCSV(line);
            if (existing.getName().equalsIgnoreCase(name) && existing.getSeason().equalsIgnoreCase(season)) {
                System.out.println("Error: A collection with that name and season already exists.");
                return;
            }
        }

        int id = FileManager.nextId(FILE);
        Collection collection = new Collection(id, name, season, releasePeriod, description);
        FileManager.appendLine(FILE, collection.toCSV());
        System.out.println("Collection created successfully: " + collection);
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
