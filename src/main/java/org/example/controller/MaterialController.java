package org.example.controller;

import org.example.model.Material;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC4: Register Material
 * Actor: Inventory Planner
 *
 * TODO (your name): Implement registerMaterial() and listMaterials().
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
        // TODO: Implement UC4 - Register Material
        //
        // Steps:
        //   1. Prompt for: name, category, unitCost, stockLevel
        //   2. Parse unitCost as double — reject if <= 0
        //   3. Parse stockLevel as int — reject if < 0
        //   4. Load existing materials; warn if same name already exists (but allow override)
        //   5. Generate ID with FileManager.nextId(FILE)
        //   6. Build Material object
        //   7. FileManager.appendLine(FILE, material.toCSV())
        //   8. Print confirmation

        System.out.println("[TODO] registerMaterial() not yet implemented.");
    }

    // -------------------------------------------------------------------------
    // Helper — List Materials
    // -------------------------------------------------------------------------

    public void listMaterials() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
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
