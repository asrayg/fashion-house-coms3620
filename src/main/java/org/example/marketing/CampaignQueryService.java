package org.example.marketing;

import org.example.model.AdCampaign;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Lists and single-campaign views. */
public class CampaignQueryService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignQueryService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void listCampaigns() {
        List<AdCampaign> all = repository.loadAll();
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

    public void viewCampaignDetails() {
        System.out.print("Campaign ID: ");
        int id = AdCampaignSupport.readInt(scanner);
        AdCampaign c = repository.findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }

        String[] pList = c.getPlatforms().split("\\|");
        String[] bList = c.getPlatformBudgets().split("\\|");
        LocalDate end = AdCampaignSupport.parseDate(c.getEndDate());
        long daysLeft = (end == null) ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), end);
        double cpc = c.getCostPerConversion();

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

        String impBar = AdCampaignSupport.generateBar(c.getImpressionProgress());
        System.out.println("    Progress       : " + impBar);

        if (!c.getNotes().isEmpty())
            System.out.println("  Notes          : " + c.getNotes());
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
