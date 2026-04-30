package org.example.marketing;

import org.example.model.AdCampaign;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/** Budget reports, analytics, scheduling checks, sync, audit trail. */
public class CampaignReportingService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignReportingService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void collectionBudgetReport() {
        List<AdCampaign> all = repository.loadAll();
        if (all.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }

        Map<Integer, List<AdCampaign>> byCollection =
                all.stream().collect(Collectors.groupingBy(AdCampaign::getCollectionId));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║        COLLECTION BUDGET REPORT              ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        for (Map.Entry<Integer, List<AdCampaign>> entry : byCollection.entrySet()) {
            int colId = entry.getKey();
            double spent = AdCampaignSupport.getTotalSpentForCollection(all, colId);
            double remaining = AdCampaignConstants.COLLECTION_BUDGET_LIMIT - spent;
            double utilPct = (spent / AdCampaignConstants.COLLECTION_BUDGET_LIMIT) * 100;
            String bar = AdCampaignSupport.generateBar(utilPct);

            System.out.println("\n  Collection [" + colId + "]");
            System.out.println("    Budget Limit : $" + String.format("%.2f", AdCampaignConstants.COLLECTION_BUDGET_LIMIT));
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

    public void platformPerformanceAnalysis() {
        List<AdCampaign> all = repository.loadAll().stream()
                .filter(c -> c.getStatus() != AdCampaign.Status.CANCELLED
                        && c.getActualImpressions() > 0)
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            System.out.println("No performance data available yet.");
            return;
        }

        Map<String, long[]> stats = new LinkedHashMap<>();
        Map<String, Double> budgetByPlatform = new LinkedHashMap<>();

        for (AdCampaign c : all) {
            String[] pList = c.getPlatforms().split("\\|");
            String[] bList = c.getPlatformBudgets().split("\\|");
            int pCount = pList.length;
            long impEach = c.getActualImpressions() / pCount;
            long convEach = c.getActualConversions() / pCount;

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
        double bestRate = -1;

        for (Map.Entry<String, long[]> e : stats.entrySet()) {
            String p = e.getKey();
            long[] s = e.getValue();
            double rate = (s[1] > 0) ? (s[2] * 100.0 / s[1]) : 0;
            double cpc = (s[2] > 0) ? (budgetByPlatform.getOrDefault(p, 0.0) / s[2]) : -1;

            System.out.println("\n  Platform: " + p);
            System.out.println("    Campaigns    : " + s[0]);
            System.out.println("    Impressions  : " + s[1]);
            System.out.println("    Conversions  : " + s[2]);
            System.out.println("    Conv. Rate   : " + String.format("%.2f", rate) + "%");
            System.out.println("    Total Budget : $" + String.format("%.2f", budgetByPlatform.getOrDefault(p, 0.0)));
            System.out.println("    Cost/Conv.   : " + (cpc < 0 ? "N/A" : "$" + String.format("%.2f", cpc)));

            if (rate > bestRate) {
                bestRate = rate;
                bestPlatform = p;
            }
        }

        System.out.println("\n  ─────────────────────────────────────────");
        System.out.println("  Best performing platform: " + bestPlatform
                + " (" + String.format("%.2f", bestRate) + "% conv. rate)");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    public void campaignRoiDashboard() {
        List<AdCampaign> all = repository.loadAll();
        if (all.isEmpty()) {
            System.out.println("No campaigns on file.");
            return;
        }

        double totalBudget = 0, totalBudgetActive = 0;
        long totalImpressions = 0, totalConversions = 0;
        int active = 0, completed = 0, planned = 0, cancelled = 0;

        for (AdCampaign c : all) {
            switch (c.getStatus()) {
                case ACTIVE -> {
                    active++;
                    totalBudgetActive += c.getTotalBudget();
                }
                case COMPLETED -> completed++;
                case PLANNED -> planned++;
                case CANCELLED -> cancelled++;
                default -> {
                }
            }
            if (c.getStatus() != AdCampaign.Status.CANCELLED) {
                totalBudget += c.getTotalBudget();
                totalImpressions += c.getActualImpressions();
                totalConversions += c.getActualConversions();
            }
        }

        double globalConvRate = (totalImpressions > 0)
                ? (totalConversions * 100.0 / totalImpressions) : 0;
        double globalCpc = (totalConversions > 0)
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

    public void schedulingConflictChecker() {
        System.out.print("Collection ID (or 0 for all): ");
        int colFilter = AdCampaignSupport.readInt(scanner);

        List<AdCampaign> all = repository.loadAll().stream()
                .filter(c -> c.getStatus() != AdCampaign.Status.CANCELLED)
                .filter(c -> colFilter == 0 || c.getCollectionId() == colFilter)
                .collect(Collectors.toList());

        List<String> conflicts = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            AdCampaign a = all.get(i);
            for (int j = i + 1; j < all.size(); j++) {
                AdCampaign b = all.get(j);
                if (a.getCollectionId() != b.getCollectionId()) continue;
                List<String> sharedPlatforms = AdCampaignSupport.sharedPlatforms(a, b);
                if (!sharedPlatforms.isEmpty() && AdCampaignSupport.datesOverlap(a, b)) {
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

    public void syncCampaignStatuses() {
        List<AdCampaign> all = repository.loadAll();
        int updated = 0;

        for (AdCampaign c : all) {
            if (c.getStatus() == AdCampaign.Status.CANCELLED
                    || c.getStatus() == AdCampaign.Status.COMPLETED) continue;

            LocalDate start = AdCampaignSupport.parseDate(c.getStartDate());
            LocalDate end = AdCampaignSupport.parseDate(c.getEndDate());
            if (start == null || end == null) continue;

            AdCampaign.Status resolved = AdCampaignSupport.resolveStatus(start, end);
            if (resolved != c.getStatus()) {
                System.out.println("  [" + c.getId() + "] " + c.getName()
                        + ": " + c.getStatus() + " → " + resolved);
                c.setStatus(resolved);
                repository.update(c);
                updated++;
            }
        }

        System.out.println(updated == 0 ? "All statuses are up to date." : updated + " campaign(s) updated.");
    }

    public void campaignAuditTrail() {
        System.out.print("Collection ID (or 0 for all): ");
        int colFilter = AdCampaignSupport.readInt(scanner);

        List<AdCampaign> all = repository.loadAll().stream()
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
}
