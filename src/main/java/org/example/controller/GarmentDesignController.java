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
 * TODO Maria Sanchez: Implement addGarmentDesign() and listGarments().
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
    // Step 1: Prompt for collection ID
    System.out.print("Collection ID: ");
    String collectionIdInput = scanner.nextLine().trim();
    
    if (collectionIdInput.isEmpty()) {
        System.out.println("Error: Collection ID is required.");
        return;
    }
    
    int collectionId;
    try {
        collectionId = Integer.parseInt(collectionIdInput);
    } catch (NumberFormatException e) {
        System.out.println("Error: Invalid collection ID. Please enter a number.");
        return;
    }
    
    // Step 2: Verify collection exists
    Collection collection = CollectionController.findById(collectionId);
    if (collection == null) {
        System.out.println("Error: Collection with ID " + collectionId + " does not exist.");
        return;
    }
    
    // Step 3: Prompt for garment details
    System.out.print("Garment name: ");
    String name = scanner.nextLine().trim();
    
    System.out.print("Type (e.g., Dress, Shirt, Pants): ");
    String type = scanner.nextLine().trim();
    
    System.out.print("Style (e.g., Casual, Formal, Sporty): ");
    String style = scanner.nextLine().trim();
    
    System.out.print("Target audience (e.g., Men, Women, Kids): ");
    String targetAudience = scanner.nextLine().trim();
    
    System.out.print("Notes (optional): ");
    String notes = scanner.nextLine().trim();
    
    // Step 4: Validate required fields
    if (name.isEmpty() || type.isEmpty() || style.isEmpty() || targetAudience.isEmpty()) {
        System.out.println("Error: Name, type, style, and target audience are required fields.");
        return;
    }
    
    // Step 5: Check for duplicate garment name in the same collection
    List<String> lines = FileManager.readLines(FILE);
    for (String line : lines) {
        GarmentDesign existing = GarmentDesign.fromCSV(line);
        if (existing.getCollectionId() == collectionId && 
            existing.getName().equalsIgnoreCase(name)) {
            System.out.println("Error: A garment with the name '" + name + 
                             "' already exists in collection '" + collection.getName() + "'.");
            return;
        }
    }
    
    // Step 6: Generate new ID
    int id = FileManager.nextId(FILE);
    
    // Step 7: Build GarmentDesign object
    GarmentDesign garment = new GarmentDesign(id, collectionId, name, type, 
                                              style, targetAudience, notes);
    
    // Step 8: Save to file
    FileManager.appendLine(FILE, garment.toCSV());
    
    // Step 9: Print confirmation
    System.out.println("Garment design added successfully!");
    System.out.println("Collection: " + collection.getName() + " (ID: " + collectionId + ")");
    System.out.println("Garment: " + garment);
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
