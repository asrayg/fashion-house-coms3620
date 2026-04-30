package org.example.controller;

import org.example.model.ComplianceViolation;
import org.example.pattern.observer.ComplianceNotificationCenter;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Compliance Violation Controller
 * Manages compliance violations found during audits.
 * Uses Observer pattern to notify departments of violations via ComplianceNotificationCenter.
 */
public class ComplianceViolationController {

    private static final String VIOLATION_FILE = "data/legal/compliance_violations.csv";
    private static final List<String> VALID_DEPARTMENTS = Arrays.asList(
        "Production", "Design", "Finance", "HR", "Marketing", "Sales", "Materials", "Partnerships"
    );
    private final Scanner scanner;

    public ComplianceViolationController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void recordViolation() {
        System.out.println("\n--- Record Compliance Violation ---");

        System.out.print("Audit ID: ");
        int auditId;
        try {
            auditId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid audit ID.");
            return;
        }

        System.out.print("Violation Type (e.g., Inadequate Break Schedules, Wage Compliance Issue): ");
        String violationType = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Severity (LOW/MEDIUM/HIGH/CRITICAL): ");
        ComplianceViolation.Severity severity;
        try {
            severity = ComplianceViolation.Severity.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid severity.");
            return;
        }

        String affectedDepartment = getValidatedDepartment();

        System.out.print("Remediation Due Date (YYYY-MM-DD): ");
        String remediationDueDate = scanner.nextLine().trim();

        System.out.print("Notes: ");
        String notes = scanner.nextLine().trim();

        List<String> lines = FileManager.readLines(VIOLATION_FILE);
        int nextId = lines.isEmpty() ? 1 : Integer.parseInt(lines.get(lines.size() - 1).split(",")[0]) + 1;

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        ComplianceViolation violation = new ComplianceViolation(
            nextId,
            auditId,
            violationType,
            description,
            severity,
            affectedDepartment,
            ComplianceViolation.Status.REPORTED,
            remediationDueDate,
            "",
            notes,
            today
        );

        FileManager.appendLine(VIOLATION_FILE, violation.toCSV());
        System.out.println("* Compliance violation recorded with ID: " + nextId);

        // Use Observer pattern to notify departments
        ComplianceNotificationCenter.notifyViolationRecorded(violation);
    }

    public void viewViolations() {
        System.out.println("\n--- Compliance Violations ---");
        List<String> lines = FileManager.readLines(VIOLATION_FILE);

        if (lines.isEmpty()) {
            System.out.println("No violations recorded.");
            return;
        }

        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            System.out.println(violation);
            System.out.println("  Description: " + violation.getDescription());
            System.out.println("  Due Date: " + violation.getRemediationDueDate());
        }
    }

    public void updateViolationStatus() {
        System.out.println("\n--- Update Violation Status ---");
        System.out.print("Violation ID: ");
        int violationId;
        try {
            violationId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        List<String> lines = FileManager.readLines(VIOLATION_FILE);
        List<String> updated = new ArrayList<>();
        boolean found = false;

        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            if (violation.getId() == violationId) {
                found = true;
                System.out.println("Current Status: " + violation.getStatus());
                System.out.print("New Status (REPORTED/UNDER_REVIEW/REMEDIATION_IN_PROGRESS/REMEDIATED/CLOSED/DISPUTED): ");
                try {
                    ComplianceViolation.Status newStatus = ComplianceViolation.Status.valueOf(scanner.nextLine().trim().toUpperCase());
                    violation.setStatus(newStatus);
                    if (newStatus == ComplianceViolation.Status.REMEDIATED || newStatus == ComplianceViolation.Status.CLOSED) {
                        violation.setRemediationCompletionDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid status.");
                    updated.add(line);
                    continue;
                }

                // Notify observers of status change
                ComplianceNotificationCenter.notifyViolationStatusChanged(violation);
            }
            updated.add(violation.toCSV());
        }

        if (found) {
            FileManager.writeLines(VIOLATION_FILE, updated);
            System.out.println("* Violation status updated.");
        } else {
            System.out.println("Violation not found.");
        }
    }

    public void viewViolationsByDepartment() {
        System.out.println("\n--- View Violations by Department ---");
        String department = getValidatedDepartment();

        List<String> lines = FileManager.readLines(VIOLATION_FILE);
        boolean found = false;

        System.out.println("\nViolations for Department: " + department);
        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            if (department.equalsIgnoreCase(violation.getAffectedDepartment())) {
                found = true;
                System.out.println(violation);
                System.out.println("  Type: " + violation.getViolationType() + " | Due: " + violation.getRemediationDueDate());
            }
        }

        if (!found) {
            System.out.println("No violations found for department: " + department);
        }
    }

    public void viewOverdueViolations() {
        System.out.println("\n--- Overdue Violations ---");
        List<String> lines = FileManager.readLines(VIOLATION_FILE);

        LocalDate today = LocalDate.now();
        boolean found = false;

        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            try {
                LocalDate dueDate = LocalDate.parse(violation.getRemediationDueDate());
                if (dueDate.isBefore(today) && 
                    (violation.getStatus() != ComplianceViolation.Status.REMEDIATED && 
                     violation.getStatus() != ComplianceViolation.Status.CLOSED)) {
                    found = true;
                    System.out.println("OVERDUE: " + violation);
                    System.out.println("  Due Date: " + violation.getRemediationDueDate() + " | Days Overdue: " + 
                        (today.toEpochDay() - dueDate.toEpochDay()));
                }
            } catch (Exception e) {
                // Skip invalid date formats
            }
        }

        if (!found) {
            System.out.println("No overdue violations.");
        }
    }

    public void generateViolationReport() {
        System.out.println("\n--- Compliance Violation Report ---");
        List<String> lines = FileManager.readLines(VIOLATION_FILE);

        if (lines.isEmpty()) {
            System.out.println("No violations to report.");
            return;
        }

        int total = lines.size();
        int reported = 0, underReview = 0, remediationInProgress = 0, remediated = 0, closed = 0, disputed = 0;
        int low = 0, medium = 0, high = 0, critical = 0;

        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            switch (violation.getStatus()) {
                case REPORTED -> reported++;
                case UNDER_REVIEW -> underReview++;
                case REMEDIATION_IN_PROGRESS -> remediationInProgress++;
                case REMEDIATED -> remediated++;
                case CLOSED -> closed++;
                case DISPUTED -> disputed++;
            }
            switch (violation.getSeverity()) {
                case LOW -> low++;
                case MEDIUM -> medium++;
                case HIGH -> high++;
                case CRITICAL -> critical++;
            }
        }

        System.out.println("\n=== VIOLATION SUMMARY ===");
        System.out.println("Total Violations: " + total);
        System.out.println("\nStatus Breakdown:");
        System.out.println("  Reported: " + reported + " | Under Review: " + underReview + 
                          " | Remediation In Progress: " + remediationInProgress);
        System.out.println("  Remediated: " + remediated + " | Closed: " + closed + " | Disputed: " + disputed);
        System.out.println("\nSeverity Breakdown:");
        System.out.println("  Low: " + low + " | Medium: " + medium + " | High: " + high + " | Critical: " + critical);
        System.out.println("\nResolution Rate: " + String.format("%.1f%%", ((remediated + closed) * 100.0 / total)));
        System.out.println("Critical/High Rate: " + String.format("%.1f%%", ((critical + high) * 100.0 / total)));
    }

    public static ComplianceViolation findById(int id) {
        List<String> lines = FileManager.readLines("data/legal/compliance_violations.csv");
        for (String line : lines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            if (violation.getId() == id) {
                return violation;
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
