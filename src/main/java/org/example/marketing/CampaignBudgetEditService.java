package org.example.marketing;

import org.example.marketing.strategy.BudgetAllocation;
import org.example.model.AdCampaign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Edit platforms and budgets; uses the same Strategy + {@link BudgetAllocation.Context}
 * as {@link CampaignLaunchService} instead of duplicating if/else allocation logic.
 */
public class CampaignBudgetEditService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignBudgetEditService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void editCampaignBudget() {
        System.out.print("Campaign ID: ");
        int id = AdCampaignSupport.readInt(scanner);
        AdCampaign c = repository.findById(id);
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
            platformList = new ArrayList<>(Arrays.asList(c.getPlatforms().split("\\|")));
        } else {
            platformList = AdCampaignSupport.parsePlatforms(pInput);
            if (platformList == null) {
                System.out.println("Error: Duplicate platforms.");
                return;
            }
        }

        System.out.print("Split evenly? (y/n): ");
        String choice = scanner.nextLine().trim();

        BudgetAllocation.Strategy strategy = choice.equalsIgnoreCase("y")
                ? new BudgetAllocation.EvenSplit()
                : new BudgetAllocation.PerPlatform();
        BudgetAllocation.Context budgetContext = new BudgetAllocation.Context(strategy);

        double inputTotal = 0;
        if (choice.equalsIgnoreCase("y")) {
            System.out.print("New total budget ($): ");
            try {
                inputTotal = Double.parseDouble(scanner.nextLine().trim());
                if (inputTotal <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid budget.");
                return;
            }
        }

        List<Double> budgets = budgetContext.allocate(platformList, inputTotal, scanner);
        if (budgets == null) {
            System.out.println("Error: Budget allocation failed.");
            return;
        }

        double newTotal = budgetContext.sumBudgets(budgets);

        List<AdCampaign> all = repository.loadAll();
        double alreadySpent = AdCampaignSupport.getTotalSpentForCollection(all, c.getCollectionId());
        double remaining =
                AdCampaignConstants.COLLECTION_BUDGET_LIMIT - (alreadySpent - c.getTotalBudget());
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
        repository.update(c);
        System.out.println("Campaign updated successfully.");
    }
}
