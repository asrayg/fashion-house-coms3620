package org.example.marketing;

import org.example.model.AdCampaign;

import java.util.Scanner;

/** Recording impressions and conversions. */
public class CampaignPerformanceService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignPerformanceService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void recordPerformance() {
        System.out.print("Campaign ID: ");
        int id = AdCampaignSupport.readInt(scanner);
        AdCampaign c = repository.findById(id);
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
        int impressions = AdCampaignSupport.readInt(scanner);
        if (impressions < 0) {
            System.out.println("Error: Impressions cannot be negative.");
            return;
        }

        System.out.print("New total conversions: ");
        int conversions = AdCampaignSupport.readInt(scanner);
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

        if (impressions >= c.getTargetImpressions()
                && conversions >= c.getTargetConversions()
                && c.getStatus() == AdCampaign.Status.ACTIVE) {
            c.setStatus(AdCampaign.Status.COMPLETED);
            System.out.println("All targets met! Campaign automatically marked COMPLETED.");
        }

        repository.update(c);

        System.out.println("Campaign launched successfully:");
        System.out.printf("Performance recorded. Conv. Rate: %.2f%% | Cost/Conv: %s%n",
                c.getConversionRate(),
                c.getCostPerConversion() < 0 ? "N/A" : "$" + String.format("%.2f", c.getCostPerConversion()));
    }
}
