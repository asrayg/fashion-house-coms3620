package org.example.controller;

import org.example.model.ComplianceAudit;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Compliance Audit Controller
 * Manages compliance audit records and reporting.
 */
public class ComplianceAuditController {

    private static final String AUDIT_FILE = "data/legal/compliance_audits.csv";
    private static final List<String> VALID_DEPARTMENTS = Arrays.asList(
        "Production", "Design", "Finance", "HR", "Marketing", "Sales", "Materials", "Partnerships"
    );
    private final Scanner scanner;

    public ComplianceAuditController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void recordComplianceAudit() {
        System.out.println("\n--- Record Compliance Audit ---");

        System.out.print("Audit Type (LABOR_STANDARDS/ENVIRONMENTAL/SAFETY/DATA_PRIVACY/FINANCIAL/SUPPLIER/OTHER): ");
        ComplianceAudit.AuditType auditType;
        try {
            auditType = ComplianceAudit.AuditType.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid audit type.");
            return;
        }

        String department = getValidatedDepartment();

        System.out.print("Findings/Summary: ");
        String findings = scanner.nextLine().trim();

        System.out.print("Severity (LOW/MEDIUM/HIGH/CRITICAL): ");
        ComplianceAudit.Severity severity;
        try {
            severity = ComplianceAudit.Severity.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid severity.");
            return;
        }

        System.out.print("Assigned To (employee name): ");
        String assignedTo = scanner.nextLine().trim();

        System.out.print("Due Date (YYYY-MM-DD): ");
        String dueDate = scanner.nextLine().trim();

        System.out.print("Notes: ");
        String notes = scanner.nextLine().trim();

        List<String> lines = FileManager.readLines(AUDIT_FILE);
        int nextId = lines.isEmpty() ? 1 : Integer.parseInt(lines.get(lines.size() - 1).split(",")[0]) + 1;

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        ComplianceAudit audit = new ComplianceAudit(
            nextId,
            today,
            auditType,
            department,
            findings,
            ComplianceAudit.Status.OPEN,
            severity,
            assignedTo,
            dueDate,
            "",
            notes,
            today
        );

        FileManager.appendLine(AUDIT_FILE, audit.toCSV());
        System.out.println("* Compliance audit recorded with ID: " + nextId);
    }

    public void viewAudits() {
        System.out.println("\n--- Compliance Audits ---");
        List<String> lines = FileManager.readLines(AUDIT_FILE);

        if (lines.isEmpty()) {
            System.out.println("No audits recorded.");
            return;
        }

        for (String line : lines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            System.out.println(audit);
            System.out.println("  Type: " + audit.getAuditType() + " | Status: " + audit.getStatus() + " | Due: " + audit.getDueDate());
        }
    }

    public void updateAuditStatus() {
        System.out.println("\n--- Update Audit Status ---");
        System.out.print("Audit ID: ");
        int auditId;
        try {
            auditId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        List<String> lines = FileManager.readLines(AUDIT_FILE);
        List<String> updated = new ArrayList<>();
        boolean found = false;

        for (String line : lines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            if (audit.getId() == auditId) {
                found = true;
                System.out.println("Current Status: " + audit.getStatus());
                System.out.print("New Status (OPEN/IN_PROGRESS/RESOLVED/CLOSED): ");
                try {
                    ComplianceAudit.Status newStatus = ComplianceAudit.Status.valueOf(scanner.nextLine().trim().toUpperCase());
                    audit.setStatus(newStatus);
                    if (newStatus == ComplianceAudit.Status.CLOSED || newStatus == ComplianceAudit.Status.RESOLVED) {
                        audit.setCompletionDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid status.");
                    updated.add(line);
                    continue;
                }
            }
            updated.add(audit.toCSV());
        }

        if (found) {
            FileManager.writeLines(AUDIT_FILE, updated);
            System.out.println("* Audit status updated.");
        } else {
            System.out.println("Audit not found.");
        }
    }

    public void viewAuditsBySeverity() {
        System.out.println("\n--- View Audits by Severity ---");
        System.out.print("Severity to filter (LOW/MEDIUM/HIGH/CRITICAL): ");
        ComplianceAudit.Severity severity;
        try {
            severity = ComplianceAudit.Severity.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid severity.");
            return;
        }

        List<String> lines = FileManager.readLines(AUDIT_FILE);
        boolean found = false;

        System.out.println("\nAudits with Severity: " + severity);
        for (String line : lines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            if (audit.getSeverity() == severity) {
                found = true;
                System.out.println(audit);
            }
        }

        if (!found) {
            System.out.println("No audits found with severity: " + severity);
        }
    }

    public void generateAuditReport() {
        System.out.println("\n--- Compliance Audit Report ---");
        List<String> lines = FileManager.readLines(AUDIT_FILE);

        if (lines.isEmpty()) {
            System.out.println("No audits to report.");
            return;
        }

        int total = lines.size();
        int open = 0, inProgress = 0, resolved = 0, closed = 0;
        int low = 0, medium = 0, high = 0, critical = 0;

        for (String line : lines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            switch (audit.getStatus()) {
                case OPEN -> open++;
                case IN_PROGRESS -> inProgress++;
                case RESOLVED -> resolved++;
                case CLOSED -> closed++;
            }
            switch (audit.getSeverity()) {
                case LOW -> low++;
                case MEDIUM -> medium++;
                case HIGH -> high++;
                case CRITICAL -> critical++;
            }
        }

        System.out.println("\n=== AUDIT SUMMARY ===");
        System.out.println("Total Audits: " + total);
        System.out.println("\nStatus Breakdown:");
        System.out.println("  Open: " + open + " | In Progress: " + inProgress + " | Resolved: " + resolved + " | Closed: " + closed);
        System.out.println("\nSeverity Breakdown:");
        System.out.println("  Low: " + low + " | Medium: " + medium + " | High: " + high + " | Critical: " + critical);
        System.out.println("\nOpen Rate: " + String.format("%.1f%%", (open * 100.0 / total)));
        System.out.println("Critical/High Rate: " + String.format("%.1f%%", ((critical + high) * 100.0 / total)));
    }

    public static ComplianceAudit findById(int id) {
        List<String> lines = FileManager.readLines("data/legal/compliance_audits.csv");
        for (String line : lines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            if (audit.getId() == id) {
                return audit;
            }
        }
        return null;
    }

    private String getValidatedDepartment() {
        while (true) {
            System.out.println("\nAvailable Departments:");
            for (int i = 0; i < VALID_DEPARTMENTS.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + VALID_DEPARTMENTS.get(i));
            }
            System.out.print("Select Department (by name or number): ");
            String input = scanner.nextLine().trim();

            // Check if it's a number
            if (input.matches("\\d+")) {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= VALID_DEPARTMENTS.size()) {
                        return VALID_DEPARTMENTS.get(choice - 1);
                    }
                } catch (NumberFormatException e) {
                    // Fall through to name check
                }
            }

            // Check if it matches a department name (case-insensitive)
            for (String dept : VALID_DEPARTMENTS) {
                if (dept.equalsIgnoreCase(input)) {
                    return dept;
                }
            }

            System.out.println("Invalid department. Please select from the list above.");
        }
    }
}
