package org.example.marketing;

import org.example.controller.CollectionController;
import org.example.marketing.strategy.BudgetAllocation;
import org.example.model.AdCampaign;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

/** Primary use case: create a campaign with Strategy-based budget allocation. */
public class CampaignLaunchService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignLaunchService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void launchCampaign() {
        System.out.println("\n--- Launch Ad Campaign ---");

        System.out.print("Collection ID: ");
        int collectionId = AdCampaignSupport.readInt(scanner);
        if (collectionId == -1) return;

        if (CollectionController.findById(collectionId) == null) {
            System.out.println("Error: Collection not found. Campaign not created.");
            return;
        }

        List<AdCampaign> all = repository.loadAll();
        double alreadySpent = AdCampaignSupport.getTotalSpentForCollection(all, collectionId);
        double remaining = AdCampaignConstants.COLLECTION_BUDGET_LIMIT - alreadySpent;
        System.out.printf("  Collection budget: $%.2f limit | $%.2f used | $%.2f remaining%n",
                AdCampaignConstants.COLLECTION_BUDGET_LIMIT, alreadySpent, remaining);
        if (remaining <= 0) {
            System.out.println("Error: Collection marketing budget fully exhausted.");
            return;
        }

        System.out.print("Campaign name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Campaign name is required.");
            return;
        }

        for (AdCampaign existing : all) {
            if (existing.getCollectionId() == collectionId
                    && existing.getName().equalsIgnoreCase(name)
                    && existing.getStatus() != AdCampaign.Status.CANCELLED) {
                System.out.println("Error: A campaign with that name already exists for this collection.");
                return;
            }
        }

        System.out.print("Platforms (comma-separated, e.g. Instagram,TikTok,TV,Print): ");
        String platformInput = scanner.nextLine().trim();
        List<String> platformList = AdCampaignSupport.parsePlatforms(platformInput);
        if (platformList == null) {
            System.out.println("Error: Duplicate platforms detected. Campaign not created.");
            return;
        }
        if (platformList.isEmpty()) {
            System.out.println("Error: At least one platform is required.");
            return;
        }

        System.out.print("Start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        LocalDate parsedStart = AdCampaignSupport.parseDate(startDate);
        if (parsedStart == null) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (parsedStart.isBefore(LocalDate.now())) {
            System.out.println("Warning: Start date is in the past. Campaign will be set to ACTIVE.");
        }

        System.out.print("End date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();
        LocalDate parsedEnd = AdCampaignSupport.parseDate(endDate);
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

        System.out.print("Split budget evenly across platforms? (y/n): ");
        String splitChoice = scanner.nextLine().trim().toLowerCase();

        BudgetAllocation.Strategy strategy = splitChoice.equals("y")
                ? new BudgetAllocation.EvenSplit()
                : new BudgetAllocation.PerPlatform();

        BudgetAllocation.Context budgetContext = new BudgetAllocation.Context(strategy);

        double inputTotal = 0;
        if (splitChoice.equals("y")) {
            System.out.print("Total budget ($): ");
            try {
                inputTotal = Double.parseDouble(scanner.nextLine().trim());
                if (inputTotal <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid budget. Campaign not created.");
                return;
            }
        }

        List<Double> budgets = budgetContext.allocate(platformList, inputTotal, scanner);

        if (budgets == null) {
            System.out.println("Error: Budget allocation failed. Campaign not created.");
            return;
        }

        double total = budgetContext.sumBudgets(budgets);

        if (total > remaining) {
            System.out.printf("Error: Budget $%.2f exceeds remaining collection limit $%.2f.%n",
                    total, remaining);
            return;
        }

        System.out.print("Target impressions: ");
        int targetImpressions = AdCampaignSupport.readInt(scanner);
        if (targetImpressions <= 0) {
            System.out.println("Error: Target impressions must be positive.");
            return;
        }

        System.out.print("Target conversions: ");
        int targetConversions = AdCampaignSupport.readInt(scanner);
        if (targetConversions <= 0) {
            System.out.println("Error: Target conversions must be positive.");
            return;
        }
        if (targetConversions > targetImpressions) {
            System.out.println("Error: Target conversions cannot exceed target impressions.");
            return;
        }

        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();

        List<String> conflicts = AdCampaignSupport.detectConflicts(
                repository.loadAll(), collectionId, platformList, startDate, endDate, -1);
        if (!conflicts.isEmpty()) {
            System.out.println("\nWarning: Scheduling conflicts detected:");
            conflicts.forEach(c -> System.out.println("  " + c));
            System.out.print("Proceed anyway? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("Campaign not created.");
                return;
            }
        }

        AdCampaign.Status status = AdCampaignSupport.resolveStatus(parsedStart, parsedEnd);

        String platformsStr = String.join("|", platformList);
        StringJoiner budgetsJoiner = new StringJoiner("|");
        for (double b : budgets) budgetsJoiner.add(String.format("%.2f", b));

        int id = FileManager.nextId(AdCampaignConstants.CAMPAIGNS_FILE);
        AdCampaign campaign = new AdCampaign(
                id, collectionId, name,
                platformsStr, budgetsJoiner.toString(),
                total, startDate, endDate,
                status, targetImpressions, targetConversions,
                0, 0, notes
        );
        repository.append(campaign);

        System.out.println("\nCampaign launched successfully:");
        System.out.println(campaign);
        System.out.printf("  Remaining collection budget: $%.2f%n", remaining - total);
    }
}
