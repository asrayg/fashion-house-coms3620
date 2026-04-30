package org.example.controller;

import org.example.model.ComplianceAudit;
import org.example.model.ComplianceViolation;
import org.example.pattern.observer.ComplianceNotificationCenter;
import org.example.pattern.observer.ExecutiveComplianceObserver;
import org.example.pattern.observer.FinanceComplianceObserver;
import org.example.pattern.observer.ProductionComplianceObserver;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * Legal & Compliance Department Management — Iteration 3
 * Actor: Compliance Officer / Legal Advisor
 *
 * Consolidates compliance workflows behind a single menu entry and adds
 * department-level legal and compliance use cases with Observer pattern
 * for notifying departments of violations.
 *
 * Design Pattern: Observer Pattern
 * - ComplianceNotificationCenter acts as the Subject
 * - Departments register as Observers to receive violation notifications
 * - When violations are recorded or status changes, all observers are notified
 */
public class LegalAndComplianceController {

    private static final String AUDIT_FILE = "data/legal/compliance_audits.csv";
    private static final String VIOLATION_FILE = "data/legal/compliance_violations.csv";

    private final Scanner scanner;
    private final ComplianceAuditController complianceAuditController;
    private final ComplianceViolationController complianceViolationController;
    private boolean observersInitialized = false;

    public LegalAndComplianceController(Scanner scanner) {
        this.scanner = scanner;
        this.complianceAuditController = new ComplianceAuditController(scanner);
        this.complianceViolationController = new ComplianceViolationController(scanner);
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║    Legal & Compliance Department Management  ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Record Compliance Audit                ║");
            System.out.println("║  2.  View Compliance Audits                 ║");
            System.out.println("║  3.  Update Audit Status                    ║");
            System.out.println("║  4.  View Audits by Severity                ║");
            System.out.println("║  5.  Generate Audit Report                  ║");
            System.out.println("║  6.  Record Compliance Violation            ║");
            System.out.println("║  7.  View Compliance Violations             ║");
            System.out.println("║  8.  Update Violation Status                ║");
            System.out.println("║  9.  View Violations by Department          ║");
            System.out.println("║ 10.  View Overdue Violations                ║");
            System.out.println("║ 11.  Generate Violation Report              ║");
            System.out.println("║ 12.  Manage Compliance Observers (Pattern)  ║");
            System.out.println("║ 13.  Compliance Dashboard                   ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1"  -> complianceAuditController.recordComplianceAudit();
                case "2"  -> complianceAuditController.viewAudits();
                case "3"  -> complianceAuditController.updateAuditStatus();
                case "4"  -> complianceAuditController.viewAuditsBySeverity();
                case "5"  -> complianceAuditController.generateAuditReport();
                case "6"  -> complianceViolationController.recordViolation();
                case "7"  -> complianceViolationController.viewViolations();
                case "8"  -> complianceViolationController.updateViolationStatus();
                case "9"  -> complianceViolationController.viewViolationsByDepartment();
                case "10" -> complianceViolationController.viewOverdueViolations();
                case "11" -> complianceViolationController.generateViolationReport();
                case "12" -> manageComplianceObservers();
                case "13" -> complianceDashboard();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    /**
     * Observer Pattern implementation: Manage which departments are subscribed to compliance notifications.
     */
    private void manageComplianceObservers() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║    Compliance Observers Management (Pattern)   ║");
            System.out.println("╠════════════════════════════════════════════════╣");
            System.out.println("║  1.  Initialize Default Observers             ║");
            System.out.println("║  2.  Subscribe Production Department          ║");
            System.out.println("║  3.  Subscribe Finance Department             ║");
            System.out.println("║  4.  Subscribe Executive Administration       ║");
            System.out.println("║  5.  View Subscribed Observers                ║");
            System.out.println("║  6.  Clear All Observers                      ║");
            System.out.println("║  0.  Back                                     ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> initializeDefaultObservers();
                case "2" -> {
                    ProductionComplianceObserver prodObserver = new ProductionComplianceObserver();
                    ComplianceNotificationCenter.attach(prodObserver);
                }
                case "3" -> {
                    FinanceComplianceObserver finObserver = new FinanceComplianceObserver();
                    ComplianceNotificationCenter.attach(finObserver);
                }
                case "4" -> {
                    ExecutiveComplianceObserver execObserver = new ExecutiveComplianceObserver();
                    ComplianceNotificationCenter.attach(execObserver);
                }
                case "5" -> viewSubscribedObservers();
                case "6" -> {
                    ComplianceNotificationCenter.clearObservers();
                    System.out.println("✓ All observers cleared.");
                }
                case "0" -> managing = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void initializeDefaultObservers() {
        System.out.println("\n--- Initializing Default Observers ---");
        ComplianceNotificationCenter.clearObservers();
        
        ProductionComplianceObserver prodObserver = new ProductionComplianceObserver();
        FinanceComplianceObserver finObserver = new FinanceComplianceObserver();
        ExecutiveComplianceObserver execObserver = new ExecutiveComplianceObserver();

        ComplianceNotificationCenter.attach(prodObserver);
        ComplianceNotificationCenter.attach(finObserver);
        ComplianceNotificationCenter.attach(execObserver);

        System.out.println("✓ Default observers initialized and subscribed to compliance notifications.");
        observersInitialized = true;
    }

    private void viewSubscribedObservers() {
        List<String> observers = ComplianceNotificationCenter.getObserverNames();
        if (observers.isEmpty()) {
            System.out.println("\nNo observers subscribed to compliance notifications.");
        } else {
            System.out.println("\n--- Subscribed Observers (" + observers.size() + ") ---");
            for (int i = 0; i < observers.size(); i++) {
                System.out.println((i + 1) + ". " + observers.get(i));
            }
        }
    }

    private void complianceDashboard() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║        Compliance Department Dashboard        ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        List<String> auditLines = FileManager.readLines(AUDIT_FILE);
        List<String> violationLines = FileManager.readLines(VIOLATION_FILE);

        int totalAudits = auditLines.size();
        int totalViolations = violationLines.size();
        int auditsClosed = 0;
        int auditsCritical = 0;
        int violationOverdue = 0;
        int violationsCritical = 0;
        int violationsResolved = 0;

        // Audit analysis
        for (String line : auditLines) {
            ComplianceAudit audit = ComplianceAudit.fromCSV(line);
            if (audit.getStatus() == ComplianceAudit.Status.CLOSED) {
                auditsClosed++;
            }
            if (audit.getSeverity() == ComplianceAudit.Severity.CRITICAL) {
                auditsCritical++;
            }
        }

        // Violation analysis
        java.time.LocalDate today = java.time.LocalDate.now();
        for (String line : violationLines) {
            ComplianceViolation violation = ComplianceViolation.fromCSV(line);
            if (violation.getSeverity() == ComplianceViolation.Severity.CRITICAL) {
                violationsCritical++;
            }
            if (violation.getStatus() == ComplianceViolation.Status.REMEDIATED ||
                violation.getStatus() == ComplianceViolation.Status.CLOSED) {
                violationsResolved++;
            }
            // Check if overdue
            try {
                java.time.LocalDate dueDate = java.time.LocalDate.parse(violation.getRemediationDueDate());
                if (dueDate.isBefore(today) && 
                    (violation.getStatus() != ComplianceViolation.Status.REMEDIATED && 
                     violation.getStatus() != ComplianceViolation.Status.CLOSED)) {
                    violationOverdue++;
                }
            } catch (Exception e) {
                // Skip invalid date formats
            }
        }

        System.out.println("\n=== COMPLIANCE METRICS ===\n");
        System.out.println("AUDITS");
        System.out.println("  Total Audits: " + totalAudits);
        System.out.println("  Closed: " + auditsClosed + " (" + String.format("%.1f%%", (auditsClosed * 100.0 / Math.max(totalAudits, 1))) + ")");
        System.out.println("  Critical Audits: " + auditsCritical);

        System.out.println("\nVIOLATIONS");
        System.out.println("  Total Violations: " + totalViolations);
        System.out.println("  Resolved: " + violationsResolved + " (" + String.format("%.1f%%", (violationsResolved * 100.0 / Math.max(totalViolations, 1))) + ")");
        System.out.println("  Critical Violations: " + violationsCritical);

        System.out.println("\nOBSERVER PATTERN");
        List<String> observers = ComplianceNotificationCenter.getObserverNames();
        System.out.println("  Active Observers: " + observers.size());
        if (!observers.isEmpty()) {
            for (String observer : observers) {
                System.out.println("    • " + observer);
            }
        }

        System.out.println("\nRECOMMENDATIONS");
        if (violationsCritical > 0) {
            System.out.println("  " + violationsCritical + " CRITICAL violation(s) require immediate attention");
        }
        if (violationOverdue > 0) {
            System.out.println("  " + violationOverdue + " violation(s) are overdue for remediation");
        }
        if (observers.isEmpty()) {
            System.out.println("  Suggest initializing observers to enable department notifications");
        }
    }
}
