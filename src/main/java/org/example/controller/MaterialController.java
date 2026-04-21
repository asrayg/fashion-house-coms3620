package org.example.controller;

import org.example.model.Material;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC4: Register Material
 * Actor: Inventory Planner
 *
 * TODO Billy Dang: Implement registerMaterial() and listMaterials().
 */
public class MaterialController {

    static final String FILE = "data/materials.csv";

    private final Scanner scanner;

    public MaterialController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Material Registry ---");
            System.out.println("1. Register Material");
            System.out.println("2. List All Materials");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> registerMaterial();
                case "2" -> listMaterials();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Use Case 4 — Register Material
    // -------------------------------------------------------------------------

    private void registerMaterial() {
        System.out.print("Material name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Unit cost: ");
        double unitCost;
        try {
            unitCost = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Unit cost must be a number.");
            return;
        }

        System.out.print("Stock level: ");
        int stockLevel;
        try {
            stockLevel = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Stock level must be a whole number.");
            return;
        }

        if (name.isEmpty() || category.isEmpty()) {
            System.out.println("Error: Material name and category are required.");
            return;
        }
        if (unitCost <= 0) {
            System.out.println("Error: Unit cost must be greater than 0.");
            return;
        }
        if (stockLevel < 0) {
            System.out.println("Error: Stock level cannot be negative.");
            return;
        }

        List<String> lines = FileManager.readLines(FILE);
        for (String line : lines) {
            Material existing = Material.fromCSV(line);
            if (existing.getName().equalsIgnoreCase(name)) {
                System.out.println("Warning: A material with this name already exists.");
                break;
            }
        }

        int id = FileManager.nextId(FILE);
        Material material = new Material(id, name, category, unitCost, stockLevel);
        FileManager.appendLine(FILE, material.toCSV());

        System.out.println("Material registered successfully: " + material);
    }

    // -------------------------------------------------------------------------
    // Helper — List Materials
    // -------------------------------------------------------------------------

    public void listMaterials() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
// [MermaidChart: 7ff4e30c-623b-4944-a514-da5e3c87642d]
// [MermaidChart: 7ff4e30c-623b-4944-a514-da5e3c87642d]
            System.out.println("No materials on file.");
            return;
        }
        System.out.println("\n--- Materials ---");
        for (String line : lines) {
            System.out.println(Material.fromCSV(line));
        }
    }

    /**
     * Used by MaterialOrderController and iteration 2 to verify a material exists.
     */
    public static Material findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            Material m = Material.fromCSV(line);
            if (m.getId() == id) return m;
        }
        return null;
    }

    /**
     * Persist an updated Material object (replaces its line in the file).
     * Iteration 2 uses this when recording material usage or receiving shipments.
     */
    public static void update(Material updated) {
        List<String> lines = FileManager.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            Material m = Material.fromCSV(lines.get(i));
            if (m.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(FILE, lines);
    }
}
