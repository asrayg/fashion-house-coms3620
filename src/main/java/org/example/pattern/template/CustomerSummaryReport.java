package org.example.pattern.template;

import org.example.model.CustomerComplaint;
import org.example.model.CustomerFeedback;
import org.example.model.CustomerProfile;
import org.example.util.FileManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Concrete Template Method report — aggregate summary across all customers.
 *
 * Skeleton inherited from CustomerReport:
 *   loadData()    → reads all four Customer Relations CSVs without filtering
 *   printHeader() → Unicode box titled "Customer Relations — Summary Report"
 *   printBody()   → tier breakdown, complaint severity/status breakdown, top category
 *   printFooter() → average satisfaction score across all feedback (overrides hook)
 */
public class CustomerSummaryReport extends CustomerReport {

    private static final String PROFILE_FILE   = "data/customer_relations/customer_profiles.csv";
    private static final String COMPLAINT_FILE = "data/customer_relations/customer_complaints.csv";
    private static final String FEEDBACK_FILE  = "data/customer_relations/customer_feedback.csv";

    private List<CustomerProfile> profiles;
    private List<CustomerComplaint> complaints;
    private List<CustomerFeedback> feedbacks;

    @Override
    protected void loadData() {
        profiles   = FileManager.readLines(PROFILE_FILE).stream()
            .map(CustomerProfile::fromCSV).collect(Collectors.toList());
        complaints = FileManager.readLines(COMPLAINT_FILE).stream()
            .map(CustomerComplaint::fromCSV).collect(Collectors.toList());
        feedbacks  = FileManager.readLines(FEEDBACK_FILE).stream()
            .map(CustomerFeedback::fromCSV).collect(Collectors.toList());
    }

    @Override
    protected void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║      CUSTOMER RELATIONS — SUMMARY REPORT         ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
    }

    @Override
    protected void printBody() {
        // Tier breakdown
        long standard = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.STANDARD).count();
        long premium  = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.PREMIUM).count();
        long vip      = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.VIP).count();

        System.out.println("  CUSTOMERS (" + profiles.size() + " total):");
        System.out.println("    STANDARD : " + standard);
        System.out.println("    PREMIUM  : " + premium);
        System.out.println("    VIP      : " + vip);
        System.out.println("  ──────────────────────────────────────────────");

        // Complaint status breakdown
        long open      = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.OPEN).count();
        long escalated = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.ESCALATED).count();
        long resolved  = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.RESOLVED).count();

        System.out.println("  COMPLAINTS (" + complaints.size() + " total):");
        System.out.println("    Open      : " + open);
        System.out.println("    Escalated : " + escalated);
        System.out.println("    Resolved  : " + resolved);
        System.out.println("  ──────────────────────────────────────────────");

        // Severity breakdown
        System.out.println("  BY SEVERITY:");
        for (CustomerComplaint.Severity s : CustomerComplaint.Severity.values()) {
            long count = complaints.stream().filter(c -> c.getSeverity() == s).count();
            System.out.println("    " + s + " : " + count);
        }
        System.out.println("  ──────────────────────────────────────────────");

        // Top complaint category
        if (!complaints.isEmpty()) {
            Map<CustomerComplaint.Category, Long> categoryCounts = complaints.stream()
                .collect(Collectors.groupingBy(CustomerComplaint::getCategory, Collectors.counting()));
            CustomerComplaint.Category topCategory = categoryCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
            System.out.println("  Top complaint category: "
                             + (topCategory != null ? topCategory : "N/A"));
        }
    }

    @Override
    protected void printFooter() {
        double avgScore = feedbacks.stream()
            .mapToInt(CustomerFeedback::getSatisfactionScore).average().orElse(0.0);

        System.out.println("  ──────────────────────────────────────────────");
        if (feedbacks.isEmpty()) {
            System.out.println("  No feedback recorded yet.");
        } else {
            System.out.printf("  Avg satisfaction score : %.1f / 5.0  (%d response(s))%n",
                              avgScore, feedbacks.size());
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
