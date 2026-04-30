package org.example.controller;

import org.example.model.CustomerComplaint;
import org.example.model.CustomerFeedback;
import org.example.model.CustomerProfile;
import org.example.pattern.template.ComplaintHistoryReport;
import org.example.pattern.template.CustomerSummaryReport;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Customer Relations Department — Iteration 3
 * Actor: Customer Relations Agent / Manager
 * Code Owner: Vinayak Trigunayat
 *
 * Design Pattern: Template Method
 *   CustomerReport (abstract) defines the fixed report skeleton:
 *     generate() → loadData() → printHeader() → printBody() → printFooter()
 *   Two concrete subclasses fill in the variable steps:
 *     ComplaintHistoryReport — per-customer complaint + audit trail view
 *     CustomerSummaryReport  — cross-customer aggregate statistics
 *   The controller calls report.generate() and the abstract class drives the sequence.
 *
 * Use Cases:
 *   UC-CR1  Register Customer Profile        (required)
 *   UC-CR2  Log Customer Complaint
 *   UC-CR3  Generate Customer Report         (Template Method)
 *   UC-CR4  Resolve Complaint               (simple manual)
 *   UC-CR5  Escalate Complaint
 *   UC-CR6  View Complaint Resolution History
 *   UC-CR7  Record Customer Feedback
 *   UC-CR8  View Customer Relations Dashboard
 */
public class CustomerRelationsDepartmentController {

    static final String PROFILE_FILE   = "data/customer_relations/customer_profiles.csv";
    static final String COMPLAINT_FILE = "data/customer_relations/customer_complaints.csv";
    static final String FEEDBACK_FILE  = "data/customer_relations/customer_feedback.csv";
    static final String AUDIT_FILE     = "data/customer_relations/complaint_audit_log.csv";

    private final Scanner scanner;

    public CustomerRelationsDepartmentController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║       Customer Relations Management         ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Register Customer Profile              ║");
            System.out.println("║  2.  List All Customers                     ║");
            System.out.println("║  3.  Log Customer Complaint                 ║");
            System.out.println("║  4.  Generate Customer Report               ║");
            System.out.println("║  5.  Resolve Complaint                      ║");
            System.out.println("║  6.  Escalate Complaint                     ║");
            System.out.println("║  7.  View Complaint Resolution History      ║");
            System.out.println("║  8.  Record Customer Feedback               ║");
            System.out.println("║  9.  Customer Relations Dashboard           ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> registerCustomerProfile();
                case "2" -> listAllCustomers();
                case "3" -> logCustomerComplaint();
                case "4" -> generateCustomerReport();
                case "5" -> resolveComplaint();
                case "6" -> escalateComplaint();
                case "7" -> viewResolutionHistory();
                case "8" -> recordCustomerFeedback();
                case "9" -> customerRelationsDashboard();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // UC-CR1: Register Customer Profile
    // =========================================================================

    private void registerCustomerProfile() {
        System.out.println("\n--- Register Customer Profile ---");

        String fullName = readNonEmpty("Full name: ");
        if (fullName == null) return;

        String email = readNonEmpty("Email address: ");
        if (email == null) return;
        if (!email.contains("@")) {
            System.out.println("Error: Invalid email address.");
            return;
        }

        for (CustomerProfile existing : loadAllProfiles()) {
            if (existing.getEmail().equalsIgnoreCase(email)) {
                System.out.println("Error: A profile with that email already exists (ID: "
                        + existing.getId() + ").");
                return;
            }
        }

        String phone = readNonEmpty("Phone number: ");
        if (phone == null) return;

        System.out.println("Preferred contact method:");
        System.out.println("  1. EMAIL");
        System.out.println("  2. PHONE");
        System.out.println("  3. EITHER");
        System.out.print("Select: ");
        CustomerProfile.PreferredContact preferredContact = switch (scanner.nextLine().trim()) {
            case "1" -> CustomerProfile.PreferredContact.EMAIL;
            case "2" -> CustomerProfile.PreferredContact.PHONE;
            case "3" -> CustomerProfile.PreferredContact.EITHER;
            default  -> null;
        };
        if (preferredContact == null) { System.out.println("Invalid selection."); return; }

        System.out.println("Customer tier:");
        System.out.println("  1. STANDARD");
        System.out.println("  2. PREMIUM");
        System.out.println("  3. VIP");
        System.out.print("Select: ");
        CustomerProfile.Tier tier = switch (scanner.nextLine().trim()) {
            case "1" -> CustomerProfile.Tier.STANDARD;
            case "2" -> CustomerProfile.Tier.PREMIUM;
            case "3" -> CustomerProfile.Tier.VIP;
            default  -> null;
        };
        if (tier == null) { System.out.println("Invalid selection."); return; }

        int id = FileManager.nextId(PROFILE_FILE);
        CustomerProfile profile = new CustomerProfile(id, fullName, email, phone,
                                                       preferredContact, tier,
                                                       LocalDate.now().toString());
        FileManager.appendLine(PROFILE_FILE, profile.toCSV());
        System.out.println("Customer profile registered: " + profile);
    }

    // =========================================================================
    // List All Customers
    // =========================================================================

    private void listAllCustomers() {
        List<CustomerProfile> profiles = loadAllProfiles();
        if (profiles.isEmpty()) {
            System.out.println("No customer profiles on file.");
            return;
        }
        System.out.println("\n--- All Customers (" + profiles.size() + ") ---");
        for (CustomerProfile p : profiles) {
            long total    = loadAllComplaints().stream().filter(c -> c.getProfileId() == p.getId()).count();
            long open     = loadAllComplaints().stream().filter(c -> c.getProfileId() == p.getId()
                              && c.getStatus() == CustomerComplaint.Status.OPEN).count();
            long resolved = loadAllComplaints().stream().filter(c -> c.getProfileId() == p.getId()
                              && c.getStatus() == CustomerComplaint.Status.RESOLVED).count();
            System.out.println("  " + p
                             + " | Complaints: " + total
                             + " (Open: " + open + ", Resolved: " + resolved + ")");
        }
    }

    // =========================================================================
    // UC-CR2: Log Customer Complaint
    // =========================================================================

    private void logCustomerComplaint() {
        System.out.println("\n--- Log Customer Complaint ---");

        if (!FileManager.hasRecords(PROFILE_FILE)) {
            System.out.println("Error: No customer profiles on file. Register one first (option 1).");
            return;
        }

        System.out.println("\n--- Registered Customers ---");
        for (CustomerProfile p : loadAllProfiles()) System.out.println("  " + p);

        Integer profileId = readId("\nEnter Customer Profile ID: ");
        if (profileId == null) return;
        if (findProfileById(profileId) == null) {
            System.out.println("Error: Profile not found.");
            return;
        }

        String description = readNonEmpty("Complaint description: ");
        if (description == null) return;

        System.out.println("Category:");
        System.out.println("  1. PRODUCT_QUALITY  2. DELIVERY  3. BILLING  4. SERVICE  5. OTHER");
        System.out.print("Select: ");
        CustomerComplaint.Category category = switch (scanner.nextLine().trim()) {
            case "1" -> CustomerComplaint.Category.PRODUCT_QUALITY;
            case "2" -> CustomerComplaint.Category.DELIVERY;
            case "3" -> CustomerComplaint.Category.BILLING;
            case "4" -> CustomerComplaint.Category.SERVICE;
            case "5" -> CustomerComplaint.Category.OTHER;
            default  -> null;
        };
        if (category == null) { System.out.println("Invalid selection."); return; }

        System.out.println("Severity:");
        System.out.println("  1. LOW   2. MEDIUM   3. HIGH   4. CRITICAL");
        System.out.print("Select: ");
        CustomerComplaint.Severity severity = switch (scanner.nextLine().trim()) {
            case "1" -> CustomerComplaint.Severity.LOW;
            case "2" -> CustomerComplaint.Severity.MEDIUM;
            case "3" -> CustomerComplaint.Severity.HIGH;
            case "4" -> CustomerComplaint.Severity.CRITICAL;
            default  -> null;
        };
        if (severity == null) { System.out.println("Invalid selection."); return; }

        int id = FileManager.nextId(COMPLAINT_FILE);
        CustomerComplaint complaint = new CustomerComplaint(id, profileId,
                description.replace(",", ";"), category, severity,
                CustomerComplaint.Status.OPEN, LocalDate.now().toString());
        FileManager.appendLine(COMPLAINT_FILE, complaint.toCSV());
        System.out.println("Complaint logged: " + complaint);
    }

    // =========================================================================
    // UC-CR3: Generate Customer Report — Template Method
    // =========================================================================

    private void generateCustomerReport() {
        System.out.println("\n--- Generate Customer Report ---");
        System.out.println("  1. Complaint History Report  (one customer — complaints, audit, feedback)");
        System.out.println("  2. Customer Summary Report   (all customers — tier breakdown, statistics)");
        System.out.print("Select report type: ");

        switch (scanner.nextLine().trim()) {
            case "1" -> {
                Integer profileId = readId("Enter Customer Profile ID: ");
                if (profileId == null) return;
                if (findProfileById(profileId) == null) {
                    System.out.println("Error: Profile not found.");
                    return;
                }
                new ComplaintHistoryReport(profileId).generate();
            }
            case "2" -> new CustomerSummaryReport().generate();
            default  -> System.out.println("Invalid report type.");
        }
    }

    // =========================================================================
    // UC-CR4: Resolve Complaint (simple manual)
    // =========================================================================

    private void resolveComplaint() {
        System.out.println("\n--- Resolve Complaint ---");

        List<CustomerComplaint> resolvable = loadAllComplaints().stream()
            .filter(c -> c.getStatus() == CustomerComplaint.Status.OPEN
                      || c.getStatus() == CustomerComplaint.Status.ESCALATED)
            .collect(Collectors.toList());

        if (resolvable.isEmpty()) {
            System.out.println("No OPEN or ESCALATED complaints to resolve.");
            return;
        }

        System.out.println("\n--- Complaints Available for Resolution ---");
        for (CustomerComplaint c : resolvable) {
            CustomerProfile p = findProfileById(c.getProfileId());
            System.out.println("  " + c + " | Customer: "
                             + (p != null ? p.getFullName() : "Unknown"));
        }

        Integer complaintId = readId("\nEnter Complaint ID: ");
        if (complaintId == null) return;
        CustomerComplaint complaint = findComplaintById(complaintId);
        if (complaint == null) { System.out.println("Error: Complaint not found."); return; }
        if (complaint.getStatus() == CustomerComplaint.Status.RESOLVED) {
            System.out.println("Error: Complaint is already resolved.");
            return;
        }

        String resolutionText = readNonEmpty("Resolution description: ");
        if (resolutionText == null) return;

        complaint.setStatus(CustomerComplaint.Status.RESOLVED);
        updateComplaint(complaint);

        // Write a simple audit entry
        int auditId = FileManager.nextId(AUDIT_FILE);
        String csv = auditId + "," + complaintId + ",RESOLVED,Agent,"
                   + resolutionText.replace(",", ";") + ",0.00,"
                   + java.time.LocalDateTime.now();
        FileManager.appendLine(AUDIT_FILE, csv);

        System.out.println("Complaint #" + complaintId + " marked as RESOLVED.");
    }

    // =========================================================================
    // UC-CR5: Escalate Complaint
    // =========================================================================

    private void escalateComplaint() {
        System.out.println("\n--- Escalate Complaint ---");

        List<CustomerComplaint> open = loadAllComplaints().stream()
            .filter(c -> c.getStatus() == CustomerComplaint.Status.OPEN)
            .collect(Collectors.toList());

        if (open.isEmpty()) { System.out.println("No OPEN complaints to escalate."); return; }

        System.out.println("\n--- Open Complaints ---");
        for (CustomerComplaint c : open) System.out.println("  " + c);

        Integer complaintId = readId("\nEnter Complaint ID to escalate: ");
        if (complaintId == null) return;
        CustomerComplaint complaint = findComplaintById(complaintId);
        if (complaint == null) { System.out.println("Error: Complaint not found."); return; }
        if (complaint.getStatus() != CustomerComplaint.Status.OPEN) {
            System.out.println("Error: Only OPEN complaints can be escalated. Status: "
                    + complaint.getStatus());
            return;
        }

        String reason = readNonEmpty("Escalation reason: ");
        if (reason == null) return;

        complaint.setStatus(CustomerComplaint.Status.ESCALATED);
        updateComplaint(complaint);

        int auditId = FileManager.nextId(AUDIT_FILE);
        String csv = auditId + "," + complaintId + ",ESCALATED,System,"
                   + reason.replace(",", ";") + ",0.00," + java.time.LocalDateTime.now();
        FileManager.appendLine(AUDIT_FILE, csv);

        System.out.println("Complaint #" + complaintId + " escalated.");
    }

    // =========================================================================
    // UC-CR6: View Complaint Resolution History
    // =========================================================================

    private void viewResolutionHistory() {
        System.out.println("\nFilter by:");
        System.out.println("  1. All complaints");
        System.out.println("  2. By Customer Profile ID");
        System.out.println("  3. By status");
        System.out.print("Select: ");
        String choice = scanner.nextLine().trim();

        List<CustomerComplaint> complaints = loadAllComplaints();

        if (choice.equals("2")) {
            Integer profileId = readId("Profile ID: ");
            if (profileId == null) return;
            complaints = complaints.stream()
                .filter(c -> c.getProfileId() == profileId).collect(Collectors.toList());
        } else if (choice.equals("3")) {
            System.out.print("Status (OPEN/ESCALATED/RESOLVED): ");
            try {
                CustomerComplaint.Status s =
                    CustomerComplaint.Status.valueOf(scanner.nextLine().trim().toUpperCase());
                complaints = complaints.stream()
                    .filter(c -> c.getStatus() == s).collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid status value.");
                return;
            }
        }

        if (complaints.isEmpty()) { System.out.println("No records found."); return; }

        List<String> auditLines = FileManager.readLines(AUDIT_FILE);

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       COMPLAINT RESOLUTION HISTORY               ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        int open = 0, escalated = 0, resolved = 0;
        complaints.sort(Comparator.comparingInt(CustomerComplaint::getId));

        for (CustomerComplaint c : complaints) {
            CustomerProfile p = findProfileById(c.getProfileId());
            System.out.println("\n  Complaint #" + c.getId()
                             + " | Customer: " + (p != null ? p.getFullName() : "Unknown")
                             + " | " + c.getSeverity() + " | " + c.getStatus()
                             + " | " + c.getLoggedDate());
            System.out.println("  " + c.getDescription());

            for (String al : auditLines) {
                String[] parts = al.split(",", 7);
                if (parts.length >= 7 && parts[1].trim().equals(String.valueOf(c.getId()))) {
                    System.out.println("    → [" + parts[6].trim() + "] "
                                     + parts[2].trim() + " | " + parts[4].trim());
                }
            }

            switch (c.getStatus()) {
                case OPEN      -> open++;
                case ESCALATED -> escalated++;
                case RESOLVED  -> resolved++;
            }
        }

        System.out.println("\n  ──────────────────────────────────────────────");
        System.out.println("  Total: " + complaints.size()
                         + " | Open: " + open
                         + " | Escalated: " + escalated
                         + " | Resolved: " + resolved);
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // UC-CR7: Record Customer Feedback
    // =========================================================================

    private void recordCustomerFeedback() {
        System.out.println("\n--- Record Customer Feedback ---");

        List<CustomerComplaint> resolved = loadAllComplaints().stream()
            .filter(c -> c.getStatus() == CustomerComplaint.Status.RESOLVED)
            .collect(Collectors.toList());

        if (resolved.isEmpty()) {
            System.out.println("No resolved complaints. Resolve a complaint first (option 4).");
            return;
        }

        System.out.println("\n--- Resolved Complaints ---");
        for (CustomerComplaint c : resolved) {
            CustomerProfile p = findProfileById(c.getProfileId());
            System.out.println("  Complaint #" + c.getId()
                             + " | " + (p != null ? p.getFullName() : "Unknown")
                             + " | " + c.getSeverity() + " | " + c.getLoggedDate());
        }

        Integer complaintId = readId("\nEnter Complaint ID for feedback: ");
        if (complaintId == null) return;
        CustomerComplaint complaint = findComplaintById(complaintId);
        if (complaint == null) { System.out.println("Error: Complaint not found."); return; }
        if (complaint.getStatus() != CustomerComplaint.Status.RESOLVED) {
            System.out.println("Error: Feedback can only be recorded for RESOLVED complaints.");
            return;
        }
        if (loadAllFeedback().stream().anyMatch(f -> f.getComplaintId() == complaintId)) {
            System.out.println("Error: Feedback already recorded for complaint #" + complaintId + ".");
            return;
        }

        String feedbackText = readNonEmpty("Feedback text: ");
        if (feedbackText == null) return;

        Integer score = readPositiveInt("Satisfaction score (1-5): ");
        if (score == null) return;
        if (score < 1 || score > 5) { System.out.println("Error: Score must be 1–5."); return; }

        System.out.print("Did the customer confirm the issue is resolved? (yes/no): ");
        boolean confirmed = scanner.nextLine().trim().equalsIgnoreCase("yes");

        int id = FileManager.nextId(FEEDBACK_FILE);
        CustomerFeedback feedback = new CustomerFeedback(id, complaintId, complaint.getProfileId(),
                feedbackText.replace(",", ";"), score, confirmed, LocalDate.now().toString());
        FileManager.appendLine(FEEDBACK_FILE, feedback.toCSV());
        System.out.println("Feedback recorded: " + feedback);
    }

    // =========================================================================
    // UC-CR8: Customer Relations Dashboard
    // =========================================================================

    private void customerRelationsDashboard() {
        List<CustomerProfile> profiles     = loadAllProfiles();
        List<CustomerComplaint> complaints = loadAllComplaints();
        List<CustomerFeedback> feedbacks   = loadAllFeedback();

        long standard = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.STANDARD).count();
        long premium  = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.PREMIUM).count();
        long vip      = profiles.stream().filter(p -> p.getTier() == CustomerProfile.Tier.VIP).count();
        long open      = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.OPEN).count();
        long escalated = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.ESCALATED).count();
        long resolved  = complaints.stream().filter(c -> c.getStatus() == CustomerComplaint.Status.RESOLVED).count();
        double avgScore = feedbacks.stream()
            .mapToInt(CustomerFeedback::getSatisfactionScore).average().orElse(0.0);

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║          CUSTOMER RELATIONS DASHBOARD                ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("  PROFILES: " + profiles.size()
                         + "  (STANDARD: " + standard
                         + " | PREMIUM: " + premium
                         + " | VIP: " + vip + ")");
        System.out.println("  COMPLAINTS: " + complaints.size()
                         + "  (Open: " + open
                         + " | Escalated: " + escalated
                         + " | Resolved: " + resolved + ")");
        System.out.printf("  Avg Satisfaction Score : %.1f / 5.0%n", avgScore);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // Repository helpers
    // =========================================================================

    private List<CustomerProfile> loadAllProfiles() {
        return FileManager.readLines(PROFILE_FILE).stream()
            .map(CustomerProfile::fromCSV).collect(Collectors.toList());
    }

    private List<CustomerComplaint> loadAllComplaints() {
        return FileManager.readLines(COMPLAINT_FILE).stream()
            .map(CustomerComplaint::fromCSV).collect(Collectors.toList());
    }

    private List<CustomerFeedback> loadAllFeedback() {
        return FileManager.readLines(FEEDBACK_FILE).stream()
            .map(CustomerFeedback::fromCSV).collect(Collectors.toList());
    }

    private CustomerProfile findProfileById(int id) {
        for (CustomerProfile p : loadAllProfiles()) if (p.getId() == id) return p;
        return null;
    }

    private CustomerComplaint findComplaintById(int id) {
        for (CustomerComplaint c : loadAllComplaints()) if (c.getId() == id) return c;
        return null;
    }

    private void updateComplaint(CustomerComplaint updated) {
        rewriteRow(COMPLAINT_FILE,
            line -> CustomerComplaint.fromCSV(line).getId() == updated.getId(),
            updated.toCSV());
    }

    private void rewriteRow(String filePath, Predicate<String> match, String newCsv) {
        List<String> lines = FileManager.readLines(filePath);
        for (int i = 0; i < lines.size(); i++) {
            if (match.test(lines.get(i))) { lines.set(i, newCsv); break; }
        }
        FileManager.writeLines(filePath, lines);
    }

    // =========================================================================
    // Console input helpers
    // =========================================================================

    private String readNonEmpty(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) { System.out.println("Error: Value is required."); return null; }
        return value;
    }

    private Integer readId(String prompt) {
        System.out.print(prompt);
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Error: Must be a number."); return null; }
    }

    private Integer readPositiveInt(String prompt) {
        Integer val = readId(prompt);
        if (val == null) return null;
        if (val <= 0) { System.out.println("Error: Value must be greater than 0."); return null; }
        return val;
    }
}
