package org.example.pattern.template;

import org.example.model.CustomerComplaint;
import org.example.model.CustomerFeedback;
import org.example.model.CustomerProfile;
import org.example.util.FileManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Template Method report — complaint history for one customer.
 *
 * Skeleton inherited from CustomerReport:
 *   loadData()    → reads profiles, complaints, feedback, audit log for the given profileId
 *   printHeader() → Unicode box with customer name and tier
 *   printBody()   → each complaint with its audit entries and feedback records
 *   printFooter() → counts by status and average satisfaction score (overrides hook)
 */
public class ComplaintHistoryReport extends CustomerReport {

    private static final String PROFILE_FILE   = "data/customer_relations/customer_profiles.csv";
    private static final String COMPLAINT_FILE = "data/customer_relations/customer_complaints.csv";
    private static final String FEEDBACK_FILE  = "data/customer_relations/customer_feedback.csv";
    private static final String AUDIT_FILE     = "data/customer_relations/complaint_audit_log.csv";

    private final int profileId;

    private CustomerProfile profile;
    private List<CustomerComplaint> complaints;
    private List<CustomerFeedback> feedbacks;
    private List<String> auditLines;

    public ComplaintHistoryReport(int profileId) {
        this.profileId = profileId;
    }

    @Override
    protected void loadData() {
        for (String line : FileManager.readLines(PROFILE_FILE)) {
            CustomerProfile p = CustomerProfile.fromCSV(line);
            if (p.getId() == profileId) { profile = p; break; }
        }

        complaints = FileManager.readLines(COMPLAINT_FILE).stream()
            .map(CustomerComplaint::fromCSV)
            .filter(c -> c.getProfileId() == profileId)
            .collect(Collectors.toList());

        feedbacks = FileManager.readLines(FEEDBACK_FILE).stream()
            .map(CustomerFeedback::fromCSV)
            .filter(f -> f.getProfileId() == profileId)
            .collect(Collectors.toList());

        auditLines = FileManager.readLines(AUDIT_FILE);
    }

    @Override
    protected void printHeader() {
        if (profile == null) {
            System.out.println("Error: Profile not found.");
            return;
        }
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║          COMPLAINT HISTORY REPORT                ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("  Customer : " + profile.getFullName());
        System.out.println("  Email    : " + profile.getEmail());
        System.out.println("  Tier     : " + profile.getTier());
        System.out.println("  Registered: " + profile.getRegistrationDate());
        System.out.println("  ──────────────────────────────────────────────");
    }

    @Override
    protected void printBody() {
        if (profile == null) return;

        if (complaints.isEmpty()) {
            System.out.println("  No complaints on file for this customer.");
            return;
        }

        for (CustomerComplaint c : complaints) {
            System.out.println("\n  Complaint #" + c.getId()
                             + " | " + c.getCategory()
                             + " | Severity: " + c.getSeverity()
                             + " | Status: " + c.getStatus()
                             + " | Filed: " + c.getLoggedDate());
            System.out.println("  Description: " + c.getDescription());

            // Matching audit entries
            boolean hasAudit = false;
            for (String al : auditLines) {
                String[] parts = al.split(",", 7);
                if (parts.length >= 7 && parts[1].trim().equals(String.valueOf(c.getId()))) {
                    if (!hasAudit) { System.out.println("  Audit:"); hasAudit = true; }
                    System.out.println("    [" + parts[6].trim() + "] "
                                     + parts[2].trim() + " / " + parts[3].trim()
                                     + " | " + parts[4].trim()
                                     + " | $" + parts[5].trim());
                }
            }

            // Matching feedback
            feedbacks.stream()
                .filter(f -> f.getComplaintId() == c.getId())
                .findFirst()
                .ifPresent(f -> System.out.println("  Feedback   : score=" + f.getSatisfactionScore()
                                                  + "/5 | confirmed=" + f.isIssueConfirmed()
                                                  + " | " + f.getFeedbackText()));
        }
    }

    @Override
    protected void printFooter() {
        if (profile == null) return;

        long open      = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.OPEN).count();
        long escalated = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.ESCALATED).count();
        long resolved  = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.RESOLVED).count();
        double avgScore = feedbacks.stream()
            .mapToInt(CustomerFeedback::getSatisfactionScore).average().orElse(0.0);

        System.out.println("\n  ──────────────────────────────────────────────");
        System.out.println("  TOTALS: " + complaints.size() + " complaint(s)"
                         + " | Open: " + open
                         + " | Escalated: " + escalated
                         + " | Resolved: " + resolved);
        if (!feedbacks.isEmpty()) {
            System.out.printf("  Avg satisfaction score: %.1f / 5.0%n", avgScore);
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
