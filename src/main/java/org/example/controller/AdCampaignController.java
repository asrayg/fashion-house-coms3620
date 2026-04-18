package org.example.controller;

import org.example.model.AdCampaign;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * UC: Launch Ad Campaign (Upgraded)
 * Actor: Marketing Manager
 * Code Owner: Anoop Boyal
 *
 * Supports: multi-platform, per-platform budgets, collection budget
 * enforcement, status lifecycle, date validation.
 */
public class AdCampaignController {

    static final String FILE = "data/campaigns.csv";
    // Per-collection marketing budget cap (could move to Collection model)
    private static final double COLLECTION_BUDGET_LIMIT = 50000.0;

    private final Scanner scanner;

    public AdCampaignController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Marketing & Campaigns ---");
            System.out.println("1. Launch Ad Campaign");
            System.out.println("2. List All Campaigns");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> launchCampaign();
                case "2" -> listCampaigns();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC: Launch Ad Campaign
    // -------------------------------------------------------------------------

    private void launchCampaign() {

        // Step 2-4: Collection validation
        System.out.print("Collection ID: ");
        int collectionId;
        try {
            collectionId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Collection not found. Campaign not created.");
            return;
        }

        if (CollectionController.findById(collectionId) == null) {
            System.out.println("Error: Collection not found. Campaign not created.");
            return;
        }

        // Step 5: Campaign name
        System.out.print("Campaign name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Invalid campaign data. Campaign not created.");
            return;
        }

        // Step 5-6: Platforms
        System.out.print("Platforms (comma-separated, e.g. Instagram,TikTok,TV): ");
        String platformInput = scanner.nextLine().trim();
        List<String> platformList = parsePlatforms(platformInput);
        if (platformList == null) {
            System.out.println("Error: Duplicate platforms detected. Campaign not created.");
            return;
        }
        if (platformList.isEmpty()) {
            System.out.println("Error: Invalid campaign data. Campaign not created.");
            return;
        }

        // Step 7: Date validation
        System.out.print("Start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        LocalDate parsedDate = parseDate(startDate);
        if (parsedDate == null) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (parsedDate.isBefore(LocalDate.now())) {
            System.out.println("Warning: Start date is in the past. Campaign will be set to ACTIVE.");
        }

        // Step 8-9: Budget entry
        System.out.print("Split budget evenly? (y/n): ");
        String splitChoice = scanner.nextLine().trim().toLowerCase();

        List<Double> budgets = new ArrayList<>();
        double totalBudget = 0;

        if (splitChoice.equals("y")) {
            System.out.print("Total budget: ");
            try {
                totalBudget = Double.parseDouble(scanner.nextLine().trim());
                if (totalBudget <= 0) throw new NumberFormatException();
                double perPlatform = totalBudget / platformList.size();
                for (int i = 0; i < platformList.size(); i++) budgets.add(perPlatform);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid budget value. Campaign not created.");
                return;
            }
        } else {
            for (String platform : platformList) {
                System.out.print("Budget for " + platform + ": ");
                try {
                    double b = Double.parseDouble(scanner.nextLine().trim());
                    if (b <= 0) throw new NumberFormatException();
                    budgets.add(b);
                    totalBudget += b;
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid budget value. Campaign not created.");
                    return;
                }
            }
        }

        // Step 10: Collection budget constraint
        double alreadySpent = getTotalSpentForCollection(collectionId);
        double remaining = COLLECTION_BUDGET_LIMIT - alreadySpent;
        if (totalBudget > remaining) {
            System.out.printf("Error: Budget exceeds collection limit. Remaining: $%.2f%n", remaining);
            return;
        }

        // Step 11: Resolve status
        String status = parsedDate.isAfter(LocalDate.now()) ? "PLANNED" : "ACTIVE";

        // Step 12: Serialize and save
        String platformsStr = String.join("|", platformList);
        StringBuilder budgetsStr = new StringBuilder();
        for (int i = 0; i < budgets.size(); i++) {
            if (i > 0) budgetsStr.append("|");
            budgetsStr.append(budgets.get(i));
        }

        int id = FileManager.nextId(FILE);
        AdCampaign campaign = new AdCampaign(
            id, collectionId, name,
            platformsStr, budgetsStr.toString(),
            totalBudget, startDate, status
        );
        FileManager.appendLine(FILE, campaign.toCSV());

        // Step 13: Confirm
        System.out.println("Campaign launched successfully:");
        System.out.println(campaign);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Parses and deduplicates platform input.
     * Returns null if duplicates found, empty list if input is blank.
     */
    private List<String> parsePlatforms(String input) {
        if (input.isEmpty()) return new ArrayList<>();
        String[] parts = input.split(",");
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (!seen.add(trimmed)) return null; // duplicate found
            result.add(p.trim());
        }
        return result;
    }

    /** Parses YYYY-MM-DD date. Returns null if invalid. */
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Sums total budget already spent on campaigns for a given collection. */
    private double getTotalSpentForCollection(int collectionId) {
        double total = 0;
        for (String line : FileManager.readLines(FILE)) {
            AdCampaign c = AdCampaign.fromCSV(line);
            if (c.getCollectionId() == collectionId
                    && !c.getStatus().equals("CANCELLED")) {
                total += c.getTotalBudget();
            }
        }
        return total;
    }

    private void listCampaigns() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }
        System.out.println("\n--- Campaigns ---");
        for (String line : lines) {
            System.out.println(AdCampaign.fromCSV(line));
            System.out.println();
        }
    }

    public static AdCampaign findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            AdCampaign c = AdCampaign.fromCSV(line);
            if (c.getId() == id) return c;
        }
        return null;
    }
}
