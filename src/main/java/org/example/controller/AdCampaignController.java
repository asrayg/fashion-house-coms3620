package org.example.controller;

import org.example.model.AdCampaign;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Marketing & Communications Department Management — Iteration 2
 * Actor: Marketing Manager
 * Code Owner: Anoop Boyal
 *
 * Features:
 *  - Launch multi-platform ad campaigns with per-platform budget allocation
 *  - Collection budget cap enforcement with remaining budget display
 *  - Campaign lifecycle management (PLANNED → ACTIVE → PAUSED → COMPLETED → CANCELLED)
 *  - Auto-status resolution from start/end dates
 *  - Date validation with past-date warnings and scheduling conflict detection
 *  - Performance metrics tracking (impressions, conversions, conversion rate)
 *  - ROI and cost-per-conversion analytics
 *  - Platform performance breakdown across campaigns
 *  - Budget utilization reports per collection
 *  - Campaign overlap detection on same platform
 *  - Soft cancel (status = CANCELLED, never hard delete)
 *  - Campaign audit trail
 *  - Marketing performance dashboard
 */
public class AdCampaignController {

    public static final String FILE = "data/campaigns.csv";
    private static final double COLLECTION_BUDGET_LIMIT = 50000.0;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner scanner;

    public AdCampaignController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║     Marketing & Communications Management    ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Launch Ad Campaign                     ║");
            System.out.println("║  2.  List All Campaigns                     ║");
            System.out.println("║  3.  View Campaign Details                  ║");
            System.out.println("║  4.  Update Campaign Status                 ║");
            System.out.println("║  5.  Record Campaign Performance            ║");
            System.out.println("║  6.  Edit Campaign Platforms & Budget       ║");
            System.out.println("║  7.  Cancel Campaign                        ║");
            System.out.println("║  8.  Collection Budget Report               ║");
            System.out.println("║  9.  Platform Performance Analysis          ║");
            System.out.println("║ 10.  Campaign ROI Dashboard                 ║");
            System.out.println("║ 11.  Scheduling Conflict Checker            ║");
            System.out.println("║ 12.  Sync Campaign Statuses (auto-update)   ║");
            System.out.println("║ 13.  Campaign Audit Trail                   ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> launchCampaign();
                case "2"  -> listCampaigns();
                case "3"  -> viewCampaignDetails();
                case "4"  -> updateCampaignStatus();
                case "5"  -> recordPerformance();
                case "6"  -> editCampaignBudget();
                case "7"  -> cancelCampaign();
                case "8"  -> collectionBudgetReport();
                case "9"  -> platformPerformanceAnalysis();
                case "10" -> campaignRoiDashboard();
                case "11" -> schedulingConflictChecker();
                case "12" -> syncCampaignStatuses();
                case "13" -> campaignAuditTrail();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Launch Ad Campaign  (Main UC)
    // =========================================================================

    private void launchCampaign() {
        System.out.println("\n--- Launch Ad Campaign ---");

        // Step 1: Collection validation
        System.out.print("Collection ID: ");
        int collectionId = readInt();
        if (collectionId == -1) return;

        if (CollectionController.findById(collectionId) == null) {
            System.out.println("Error: Collection not found. Campaign not created.");
            return;
        }

        double alreadySpent = getTotalSpentForCollection(collectionId);
        double remaining    = COLLECTION_BUDGET_LIMIT - alreadySpent;
        System.out.printf("  Collection budget: $%.2f limit | $%.2f used | $%.2f remaining%n",
                COLLECTION_BUDGET_LIMIT, alreadySpent, remaining);
        if (remaining <= 0) {
            System.out.println("Error: Collection marketing budget fully exhausted.");
            return;
        }

        // Step 2: Campaign name
        System.out.print("Campaign name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Campaign name is required.");
            return;
        }

        // Check duplicate name within collection
        for (AdCampaign existing : loadAll()) {
            if (existing.getCollectionId() == collectionId
                    && existing.getName().equalsIgnoreCase(name)
                    && existing.getStatus() != AdCampaign.Status.CANCELLED) {
                System.out.println("Error: A campaign with that name already exists for this collection.");
                return;
            }
        }

        // Step 3: Platforms
        System.out.print("Platforms (comma-separated, e.g. Instagram,TikTok,TV,Print): ");
        String platformInput = scanner.nextLine().trim();
        List<String> platformList = parsePlatforms(platformInput);
        if (platformList == null) {
            System.out.println("Error: Duplicate platforms detected. Campaign not created.");
            return;
        }
        if (platformList.isEmpty()) {
            System.out.println("Error: At least one platform is required.");
            return;
        }

        // Step 4: Dates
        System.out.print("Start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        LocalDate parsedStart = parseDate(startDate);
        if (parsedStart == null) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (parsedStart.isBefore(LocalDate.now())) {
            System.out.println("Warning: Start date is in the past. Campaign will be set to ACTIVE.");
        }

        System.out.print("End date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();
        LocalDate parsedEnd = parseDate(endDate);
        if (parsedEnd == null) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (!parsedEnd.isAfter(parsedStart)) {
            System.out.println("Error: End date must be after start date.");
            return;
        }

        long durationDays = ChronoUnit.DAYS.between(parsedStart, parsedEnd);
        System.out.println("  Campaign duration: " + durationDays + " days.");

        // Step 5: Budget allocation
        System.out.print("Split budget evenly across platforms? (y/n): ");
        String splitChoice = scanner.nextLine().trim().toLowerCase();

        List<Double> budgets  = new ArrayList<>();
        double       total    = 0;

        if (splitChoice.equals("y")) {
            System.out.print("Total budget ($): ");
            try {
                total = Double.parseDouble(scanner.nextLine().trim());
                if (total <= 0) throw new NumberFormatException();
                double each = total / platformList.size();
                for (int i = 0; i < platformList.size(); i++) budgets.add(each);
                System.out.printf("  $%.2f allocated per platform.%n", each);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid budget value. Campaign not created.");
                return;
            }
        } else {
            for (String platform : platformList) {
                System.out.print("Budget for " + platform + " ($): ");
                try {
                    double b = Double.parseDouble(scanner.nextLine().trim());
                    if (b <= 0) throw new NumberFormatException();
                    budgets.add(b);
                    total += b;
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid budget for " + platform + ". Campaign not created.");
                    return;
                }
            }
        }

        // Step 6: Enforce collection budget cap
        if (total > remaining) {
            System.out.printf("Error: Budget $%.2f exceeds remaining collection limit $%.2f.%n",
                    total, remaining);
            return;
        }

        // Step 7: Performance targets
        System.out.print("Target impressions: ");
        int targetImpressions = readInt();
        if (targetImpressions <= 0) {
            System.out.println("Error: Target impressions must be positive.");
            return;
        }

        System.out.print("Target conversions: ");
        int targetConversions = readInt();
        if (targetConversions <= 0) {
            System.out.println("Error: Target conversions must be positive.");
            return;
        }
        if (targetConversions > targetImpressions) {
            System.out.println("Error: Target conversions cannot exceed target impressions.");
            return;
        }

        // Step 8: Optional notes
        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        // Step 9: Scheduling conflict check
        List<String> conflicts = detectConflicts(collectionId, platformList, startDate, endDate, -1);
        if (!conflicts.isEmpty()) {
            System.out.println("\nWarning: Scheduling conflicts detected:");
            conflicts.forEach(c -> System.out.println("  " + c));
            System.out.print("Proceed anyway? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("Campaign not created.");
                return;
            }
        }

        // Step 10: Resolve status from dates
        AdCampaign.Status status = resolveStatus(parsedStart, parsedEnd);

        // Step 11: Build and save
        String platformsStr = String.join("|", platformList);
        StringJoiner budgetsJoiner = new StringJoiner("|");
        for (double b : budgets) budgetsJoiner.add(String.format("%.2f", b));

        int id = FileManager.nextId(FILE);
        AdCampaign campaign = new AdCampaign(
                id, collectionId, name,
                platformsStr, budgetsJoiner.toString(),
                total, startDate, endDate,
                status, targetImpressions, targetConversions,
                0, 0, notes
        );
        FileManager.appendLine(FILE, campaign.toCSV());

        System.out.println("\nCampaign launched successfully:");
        System.out.println(campaign);
        System.out.printf("  Remaining collection budget: $%.2f%n", remaining - total);
    }

    // =========================================================================
    // 2. List All Campaigns
    // =========================================================================

    private void listCampaigns() {
        List<AdCampaign> all = loadAll();
        if (all.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }

        System.out.println("\n--- All Campaigns ---");
        Map<AdCampaign.Status, List<AdCampaign>> grouped =
                all.stream().collect(Collectors.groupingBy(AdCampaign::getStatus));

        for (AdCampaign.Status s : AdCampaign.Status.values()) {
            List<AdCampaign> group = grouped.getOrDefault(s, Collections.emptyList());
            System.out.println("\n  ▸ " + s + " (" + group.size() + ")");
            if (group.isEmpty()) {
                System.out.println("    (none)");
            } else {
                for (AdCampaign c : group) {
                    System.out.println("    [" + c.getId() + "] " + c.getName()
                            + " | Collection: " + c.getCollectionId()
                            + " | Budget: $" + String.format("%.2f", c.getTotalBudget())
                            + " | " + c.getStartDate() + " → " + c.getEndDate()
                            + " | Platforms: " + c.getPlatforms().replace("|", ", "));
                }
            }
        }
    }

    // =========================================================================
    // 3. View Campaign Details
    // =========================================================================

    private void viewCampaignDetails() {
        System.out.print("Campaign ID: ");
        int id = readInt();
        AdCampaign c = findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }

        String[] pList = c.getPlatforms().split("\\|");
        String[] bList = c.getPlatformBudgets().split("\\|");
        LocalDate end = parseDate(c.getEndDate());
        long daysLeft = (end == null) ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), end);
        double cpc    = c.getCostPerConversion();

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║            CAMPAIGN DETAIL REPORT            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  ID             : " + c.getId());
        System.out.println("  Name           : " + c.getName());
        System.out.println("  Collection     : " + c.getCollectionId());
        System.out.println("  Status         : " + c.getStatus());
        System.out.println("  Period         : " + c.getStartDate() + " → " + c.getEndDate()
                + "  (" + (daysLeft >= 0 ? daysLeft + " days left" : "ended") + ")");
        System.out.println("  Total Budget   : $" + String.format("%.2f", c.getTotalBudget()));
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  PLATFORM BUDGETS:");
        for (int i = 0; i < pList.length; i++) {
            System.out.println("    - " + pList[i].trim() + ": $" + (i < bList.length ? bList[i].trim() : "0.00"));
        }
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  PERFORMANCE TARGETS vs ACTUALS:");
        System.out.println("    Impressions    : " + c.getActualImpressions()
                + " / " + c.getTargetImpressions()
                + "  (" + String.format("%.1f", c.getImpressionProgress()) + "%)");
        System.out.println("    Conversions    : " + c.getActualConversions()
                + " / " + c.getTargetConversions());
        System.out.println("    Conv. Rate     : " + String.format("%.2f", c.getConversionRate()) + "%");
        System.out.println("    Cost/Conv.     : " + (cpc < 0 ? "N/A" : "$" + String.format("%.2f", cpc)));

        String impBar = generateBar(c.getImpressionProgress());
        System.out.println("    Progress       : " + impBar);

        if (!c.getNotes().isEmpty())
            System.out.println("  Notes          : " + c.getNotes());
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 4. Update Campaign Status
    // =========================================================================

    private void updateCampaignStatus() {
        System.out.print("Campaign ID: ");
        int id = readInt();
        AdCampaign c = findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Error: Cannot change status of a cancelled campaign.");
            return;
        }

        System.out.println("Current status: " + c.getStatus());
        System.out.println("1. PLANNED");
        System.out.println("2. ACTIVE");
        System.out.println("3. PAUSED");
        System.out.println("4. COMPLETED");
        System.out.print("New status: ");

        AdCampaign.Status newStatus;
        switch (scanner.nextLine().trim()) {
            case "1" -> newStatus = AdCampaign.Status.PLANNED;
            case "2" -> newStatus = AdCampaign.Status.ACTIVE;
            case "3" -> newStatus = AdCampaign.Status.PAUSED;
            case "4" -> newStatus = AdCampaign.Status.COMPLETED;
            default  -> { System.out.println("Invalid option."); return; }
        }

        c.setStatus(newStatus);
        updateCampaign(c);
        System.out.println("Status updated to: " + newStatus);
    }

    // =========================================================================
    // 5. Record Campaign Performance
    // =========================================================================

    private void recordPerformance() {
        System.out.print("Campaign ID: ");
        int id = readInt();
        AdCampaign c = findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.PLANNED) {
            System.out.println("Warning: Campaign is still PLANNED and has not started yet.");
        }
        if (c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Error: Cannot record performance for a cancelled campaign.");
            return;
        }

        System.out.println("Current: " + c.getActualImpressions()
                + " impressions | " + c.getActualConversions() + " conversions");
        System.out.print("New total impressions: ");
        int impressions = readInt();
        if (impressions < 0) {
            System.out.println("Error: Impressions cannot be negative.");
            return;
        }

        System.out.print("New total conversions: ");
        int conversions = readInt();
        if (conversions < 0) {
            System.out.println("Error: Conversions cannot be negative.");
            return;
        }
        if (conversions > impressions) {
            System.out.println("Error: Conversions cannot exceed impressions.");
            return;
        }

        c.setActualImpressions(impressions);
        c.setActualConversions(conversions);

        // Auto-complete if targets met
        if (impressions >= c.getTargetImpressions()
                && conversions >= c.getTargetConversions()
                && c.getStatus() == AdCampaign.Status.ACTIVE) {
            c.setStatus(AdCampaign.Status.COMPLETED);
            System.out.println("All targets met! Campaign automatically marked COMPLETED.");
        }

        updateCampaign(c);

        System.out.println("Campaign launched successfully:");
        System.out.printf("Performance recorded. Conv. Rate: %.2f%% | Cost/Conv: %s%n",
                c.getConversionRate(),
                c.getCostPerConversion() < 0 ? "N/A" : "$" + String.format("%.2f", c.getCostPerConversion()));
    }

    // =========================================================================
    // 6. Edit Campaign Platforms & Budget
    // =========================================================================

    private void editCampaignBudget() {
        System.out.print("Campaign ID: ");
        int id = readInt();
        AdCampaign c = findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.COMPLETED
                || c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Error: Cannot edit a completed or cancelled campaign.");
            return;
        }

        System.out.println("Current platforms: " + c.getPlatforms().replace("|", ", "));
        System.out.println("Current budgets  : " + c.getPlatformBudgets().replace("|", ", "));
        System.out.println("Current total    : $" + String.format("%.2f", c.getTotalBudget()));

        System.out.print("New platforms (comma-separated, or Enter to keep): ");
        String pInput = scanner.nextLine().trim();
        List<String> platformList;
        if (pInput.isEmpty()) {
            platformList = Arrays.asList(c.getPlatforms().split("\\|"));
        } else {
            platformList = parsePlatforms(pInput);
            if (platformList == null) {
                System.out.println("Error: Duplicate platforms.");
                return;
            }
        }

        System.out.print("Split evenly? (y/n): ");
        String choice = scanner.nextLine().trim();
        List<Double> budgets = new ArrayList<>();
        double newTotal = 0;

        if (choice.equalsIgnoreCase("y")) {
            System.out.print("New total budget ($): ");
            try {
                newTotal = Double.parseDouble(scanner.nextLine().trim());
                if (newTotal <= 0) throw new NumberFormatException();
                double each = newTotal / platformList.size();
                for (int i = 0; i < platformList.size(); i++) budgets.add(each);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid budget.");
                return;
            }
        } else {
            for (String p : platformList) {
                System.out.print("Budget for " + p + ": $");
                try {
                    double b = Double.parseDouble(scanner.nextLine().trim());
                    if (b <= 0) throw new NumberFormatException();
                    budgets.add(b);
                    newTotal += b;
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid budget for " + p + ".");
                    return;
                }
            }
        }

        // Check collection budget cap accounting for this campaign's old budget
        double alreadySpent = getTotalSpentForCollection(c.getCollectionId());
        double remaining    = COLLECTION_BUDGET_LIMIT - (alreadySpent - c.getTotalBudget());
        if (newTotal > remaining) {
            System.out.printf("Error: New budget $%.2f exceeds remaining collection limit $%.2f.%n",
                    newTotal, remaining);
            return;
        }

        c.setPlatforms(String.join("|", platformList));
        StringJoiner sj = new StringJoiner("|");
        for (double b : budgets) sj.add(String.format("%.2f", b));
        c.setPlatformBudgets(sj.toString());
        c.setTotalBudget(newTotal);
        updateCampaign(c);
        System.out.println("Campaign updated successfully.");
    }

    // =========================================================================
    // 7. Cancel Campaign (Soft Delete)
    // =========================================================================

    private void cancelCampaign() {
        System.out.print("Campaign ID: ");
        int id = readInt();
        AdCampaign c = findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Campaign is already cancelled.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.COMPLETED) {
            System.out.println("Error: Cannot cancel a completed campaign.");
            return;
        }

        System.out.println("Campaign: " + c.getName() + " | Status: " + c.getStatus()
                + " | Budget: $" + String.format("%.2f", c.getTotalBudget()));
        System.out.print("Reason for cancellation (optional): ");
        String reason = scanner.nextLine().trim();
        System.out.print("Confirm cancellation? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        c.setStatus(AdCampaign.Status.CANCELLED);
        if (!reason.isEmpty()) c.setNotes("CANCELLED: " + reason);
        updateCampaign(c);

        double released = c.getTotalBudget();
        System.out.printf("Campaign cancelled. $%.2f returned to collection budget pool.%n", released);
    }

    // =========================================================================
    // 8. Collection Budget Report
    // =========================================================================

    private void collectionBudgetReport() {
        List<AdCampaign> all = loadAll();
        if (all.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }

        // Group by collection
        Map<Integer, List<AdCampaign>> byCollection =
                all.stream().collect(Collectors.groupingBy(AdCampaign::getCollectionId));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║        COLLECTION BUDGET REPORT              ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        for (Map.Entry<Integer, List<AdCampaign>> entry : byCollection.entrySet()) {
            int    colId   = entry.getKey();
            double spent   = getTotalSpentForCollection(colId);
            double remaining = COLLECTION_BUDGET_LIMIT - spent;
            double utilPct = (spent / COLLECTION_BUDGET_LIMIT) * 100;
            String bar     = generateBar(utilPct);

            System.out.println("\n  Collection [" + colId + "]");
            System.out.println("    Budget Limit : $" + String.format("%.2f", COLLECTION_BUDGET_LIMIT));
            System.out.println("    Spent        : $" + String.format("%.2f", spent));
            System.out.println("    Remaining    : $" + String.format("%.2f", remaining));
            System.out.println("    Utilization  : " + bar + " " + String.format("%.1f", utilPct) + "%");

            if (utilPct > 90)
                System.out.println("    *** WARNING: Budget nearly exhausted! ***");

            System.out.println("    Campaigns:");
            for (AdCampaign c : entry.getValue()) {
                System.out.println("      [" + c.getId() + "] " + c.getName()
                        + " | " + c.getStatus()
                        + " | $" + String.format("%.2f", c.getTotalBudget())
                        + " | " + c.getPlatforms().replace("|", ", "));
            }
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 9. Platform Performance Analysis
    // =========================================================================

    private void platformPerformanceAnalysis() {
        List<AdCampaign> all = loadAll().stream()
                .filter(c -> c.getStatus() != AdCampaign.Status.CANCELLED
                        && c.getActualImpressions() > 0)
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            System.out.println("No performance data available yet.");
            return;
        }

        // Aggregate per platform
        Map<String, long[]> stats = new LinkedHashMap<>();
        // stats[0]=campaigns, [1]=totalImpressions, [2]=totalConversions
        Map<String, Double> budgetByPlatform = new LinkedHashMap<>();

        for (AdCampaign c : all) {
            String[] pList = c.getPlatforms().split("\\|");
            String[] bList = c.getPlatformBudgets().split("\\|");
            int      pCount = pList.length;
            // Distribute actuals evenly across platforms as approximation
            long impEach  = c.getActualImpressions()  / pCount;
            long convEach = c.getActualConversions()  / pCount;

            for (int i = 0; i < pList.length; i++) {
                String p = pList[i].trim();
                stats.computeIfAbsent(p, k -> new long[3]);
                stats.get(p)[0]++;
                stats.get(p)[1] += impEach;
                stats.get(p)[2] += convEach;
                double b = (i < bList.length) ? Double.parseDouble(bList[i].trim()) : 0.0;
                budgetByPlatform.merge(p, b, Double::sum);
            }
        }

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         PLATFORM PERFORMANCE ANALYSIS            ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        String bestPlatform = null;
        double bestRate     = -1;

        for (Map.Entry<String, long[]> e : stats.entrySet()) {
            String p    = e.getKey();
            long[] s    = e.getValue();
            double rate = (s[1] > 0) ? (s[2] * 100.0 / s[1]) : 0;
            double cpc  = (s[2] > 0) ? (budgetByPlatform.getOrDefault(p, 0.0) / s[2]) : -1;

            System.out.println("\n  Platform: " + p);
            System.out.println("    Campaigns    : " + s[0]);
            System.out.println("    Impressions  : " + s[1]);
            System.out.println("    Conversions  : " + s[2]);
            System.out.println("    Conv. Rate   : " + String.format("%.2f", rate) + "%");
            System.out.println("    Total Budget : $" + String.format("%.2f", budgetByPlatform.getOrDefault(p, 0.0)));
            System.out.println("    Cost/Conv.   : " + (cpc < 0 ? "N/A" : "$" + String.format("%.2f", cpc)));

            if (rate > bestRate) {
                bestRate     = rate;
                bestPlatform = p;
            }
        }

        System.out.println("\n  ─────────────────────────────────────────");
        System.out.println("  Best performing platform: " + bestPlatform
                + " (" + String.format("%.2f", bestRate) + "% conv. rate)");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 10. Campaign ROI Dashboard
    // =========================================================================

    private void campaignRoiDashboard() {
        List<AdCampaign> all = loadAll();
        if (all.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }

        double totalBudget      = 0, totalBudgetActive = 0;
        long   totalImpressions = 0, totalConversions  = 0;
        int    active = 0, completed = 0, planned = 0, cancelled = 0;

        for (AdCampaign c : all) {
            switch (c.getStatus()) {
                case ACTIVE    -> { active++;    totalBudgetActive += c.getTotalBudget(); }
                case COMPLETED -> completed++;
                case PLANNED   -> planned++;
                case CANCELLED -> cancelled++;
                default        -> {}
            }
            if (c.getStatus() != AdCampaign.Status.CANCELLED) {
                totalBudget      += c.getTotalBudget();
                totalImpressions += c.getActualImpressions();
                totalConversions += c.getActualConversions();
            }
        }

        double globalConvRate = (totalImpressions > 0)
                ? (totalConversions * 100.0 / totalImpressions) : 0;
        double globalCpc      = (totalConversions > 0)
                ? (totalBudget / totalConversions) : -1;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║         CAMPAIGN ROI DASHBOARD               ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  CAMPAIGN STATUS SUMMARY:");
        System.out.println("    Active       : " + active);
        System.out.println("    Completed    : " + completed);
        System.out.println("    Planned      : " + planned);
        System.out.println("    Cancelled    : " + cancelled);
        System.out.println("    Total        : " + all.size());
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  BUDGET:");
        System.out.println("    Total Committed  : $" + String.format("%.2f", totalBudget));
        System.out.println("    Active Budget    : $" + String.format("%.2f", totalBudgetActive));
        System.out.println("  ─────────────────────────────────────────");
        System.out.println("  PERFORMANCE:");
        System.out.println("    Total Impressions: " + totalImpressions);
        System.out.println("    Total Conversions: " + totalConversions);
        System.out.println("    Global Conv. Rate: " + String.format("%.2f", globalConvRate) + "%");
        System.out.println("    Global Cost/Conv : "
                + (globalCpc < 0 ? "N/A" : "$" + String.format("%.2f", globalCpc)));
        System.out.println("  ─────────────────────────────────────────");

        // Top 3 by conversion rate
        System.out.println("  TOP CAMPAIGNS BY CONVERSION RATE:");
        all.stream()
                .filter(c -> c.getActualImpressions() > 0
                        && c.getStatus() != AdCampaign.Status.CANCELLED)
                .sorted((a, b) -> Double.compare(b.getConversionRate(), a.getConversionRate()))
                .limit(3)
                .forEach(c -> System.out.println("    [" + c.getId() + "] " + c.getName()
                        + " - " + String.format("%.2f", c.getConversionRate()) + "%"
                        + " | $" + String.format("%.2f", c.getTotalBudget())));

        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 11. Scheduling Conflict Checker
    // =========================================================================

    private void schedulingConflictChecker() {
        System.out.print("Collection ID (or 0 for all): ");
        int colFilter = readInt();

        List<AdCampaign> all = loadAll().stream()
                .filter(c -> c.getStatus() != AdCampaign.Status.CANCELLED)
                .filter(c -> colFilter == 0 || c.getCollectionId() == colFilter)
                .collect(Collectors.toList());

        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            AdCampaign a = all.get(i);
            for (int j = i + 1; j < all.size(); j++) {
                AdCampaign b = all.get(j);
                if (a.getCollectionId() != b.getCollectionId()) continue;
                List<String> sharedPlatforms = sharedPlatforms(a, b);
                if (!sharedPlatforms.isEmpty() && datesOverlap(a, b)) {
                    conflicts.add("  Campaign [" + a.getId() + "] \"" + a.getName()
                            + "\" overlaps with [" + b.getId() + "] \"" + b.getName()
                            + "\" on: " + sharedPlatforms
                            + " (" + a.getStartDate() + " / " + b.getStartDate() + ")");
                }
            }
        }

        if (conflicts.isEmpty()) {
            System.out.println("No scheduling conflicts found.");
        } else {
            System.out.println("\n--- Scheduling Conflicts ---");
            conflicts.forEach(System.out::println);
            System.out.println("Total conflicts: " + conflicts.size());
        }
    }

    // =========================================================================
    // 12. Sync Campaign Statuses (auto-update from dates)
    // =========================================================================

    private void syncCampaignStatuses() {
        List<AdCampaign> all = loadAll();
        int updated = 0;

        for (AdCampaign c : all) {
            if (c.getStatus() == AdCampaign.Status.CANCELLED
                    || c.getStatus() == AdCampaign.Status.COMPLETED) continue;

            LocalDate start = parseDate(c.getStartDate());
            LocalDate end   = parseDate(c.getEndDate());
            if (start == null || end == null) continue;

            AdCampaign.Status resolved = resolveStatus(start, end);
            if (resolved != c.getStatus()) {
                System.out.println("  [" + c.getId() + "] " + c.getName()
                        + ": " + c.getStatus() + " → " + resolved);
                c.setStatus(resolved);
                updateCampaign(c);
                updated++;
            }
        }

        System.out.println(updated == 0 ? "All statuses are up to date." : updated + " campaign(s) updated.");
    }

    // =========================================================================
    // 13. Campaign Audit Trail
    // =========================================================================

    private void campaignAuditTrail() {
        System.out.print("Collection ID (or 0 for all): ");
        int colFilter = readInt();

        List<AdCampaign> all = loadAll().stream()
                .filter(c -> colFilter == 0 || c.getCollectionId() == colFilter)
                .sorted(Comparator.comparingInt(AdCampaign::getId))
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            System.out.println("No campaigns found.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║            CAMPAIGN AUDIT TRAIL                  ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        for (AdCampaign c : all) {
            System.out.println("\n  [" + c.getId() + "] " + c.getName()
                    + " | Collection: " + c.getCollectionId()
                    + " | Status: " + c.getStatus());
            System.out.println("    Period   : " + c.getStartDate() + " → " + c.getEndDate());
            System.out.println("    Platforms: " + c.getPlatforms().replace("|", ", "));
            System.out.println("    Budget   : $" + String.format("%.2f", c.getTotalBudget()));
            System.out.println("    Targets  : " + c.getTargetImpressions()
                    + " imp / " + c.getTargetConversions() + " conv");
            System.out.println("    Actuals  : " + c.getActualImpressions()
                    + " imp / " + c.getActualConversions() + " conv"
                    + " (" + String.format("%.1f", c.getConversionRate()) + "% CVR)");
            if (!c.getNotes().isEmpty())
                System.out.println("    Notes    : " + c.getNotes());
        }

        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private List<String> parsePlatforms(String input) {
        if (input.isEmpty()) return new ArrayList<>();
        String[] parts = input.split(",");
        List<String> result = new ArrayList<>();
        Set<String>  seen   = new HashSet<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;
            if (!seen.add(trimmed)) return null; // duplicate
            result.add(p.trim());
        }
        return result;
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private AdCampaign.Status resolveStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(start))  return AdCampaign.Status.PLANNED;
        if (!today.isAfter(end))    return AdCampaign.Status.ACTIVE;
        return AdCampaign.Status.COMPLETED;
    }

    private double getTotalSpentForCollection(int collectionId) {
        return loadAll().stream()
                .filter(c -> c.getCollectionId() == collectionId
                        && c.getStatus() != AdCampaign.Status.CANCELLED)
                .mapToDouble(AdCampaign::getTotalBudget)
                .sum();
    }

    private List<String> detectConflicts(int colId, List<String> platforms,
                                         String start, String end, int excludeId) {
        List<String> conflicts = new ArrayList<>();
        for (AdCampaign existing : loadAll()) {
            if (existing.getId() == excludeId) continue;
            if (existing.getCollectionId() != colId) continue;
            if (existing.getStatus() == AdCampaign.Status.CANCELLED) continue;

            List<String> existingPlatforms =
                    Arrays.asList(existing.getPlatforms().split("\\|"));
            boolean overlap = platforms.stream()
                    .anyMatch(existingPlatforms::contains);
            if (overlap && datesOverlapRaw(start, end,
                    existing.getStartDate(), existing.getEndDate())) {
                conflicts.add("[" + existing.getId() + "] \"" + existing.getName()
                        + "\" runs " + existing.getStartDate() + " → " + existing.getEndDate());
            }
        }
        return conflicts;
    }

    private boolean datesOverlap(AdCampaign a, AdCampaign b) {
        return datesOverlapRaw(a.getStartDate(), a.getEndDate(),
                b.getStartDate(), b.getEndDate());
    }

    private boolean datesOverlapRaw(String s1, String e1, String s2, String e2) {
        LocalDate start1 = parseDate(s1), end1 = parseDate(e1);
        LocalDate start2 = parseDate(s2), end2 = parseDate(e2);
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    private List<String> sharedPlatforms(AdCampaign a, AdCampaign b) {
        List<String> ap = Arrays.asList(a.getPlatforms().split("\\|"));
        List<String> bp = Arrays.asList(b.getPlatforms().split("\\|"));
        return ap.stream().filter(bp::contains).collect(Collectors.toList());
    }

    private String generateBar(double pct) {
        int filled = Math.min(10, (int)(pct / 10));
        return "[" + "#".repeat(filled) + "-".repeat(10 - filled) + "]";
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return -1;
        }
    }

    private List<AdCampaign> loadAll() {
        List<AdCampaign> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) {
            if (line.trim().isEmpty()) continue;
            list.add(AdCampaign.fromCSV(line));
        }
        return list;
    }

    private void updateCampaign(AdCampaign updated) {
        List<String> lines = FileManager.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) continue;
            if (AdCampaign.fromCSV(lines.get(i)).getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(FILE, lines);
    }

    // =========================================================================
    // Static helpers for other controllers
    // =========================================================================

    public static AdCampaign findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            if (line.trim().isEmpty()) continue;
            AdCampaign c = AdCampaign.fromCSV(line);
            if (c.getId() == id) return c;
        }
        return null;
    }
}
