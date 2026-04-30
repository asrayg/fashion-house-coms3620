package org.example.pattern.legalcomplianceobserver;

import org.example.model.ComplianceViolation;

/**
 * Executive/Admin Observer — receives all compliance notifications for high-level monitoring.
 */
public class ExecutiveComplianceObserver implements ComplianceObserver {
    private String departmentName = "Executive Administration";
    private int violationsReceived = 0;

    @Override
    public void onViolationRecorded(ComplianceViolation violation) {
        violationsReceived++;
        System.out.println("  [EXECUTIVE] ALERT: Critical violation recorded!");
        System.out.println("    Type: " + violation.getViolationType() + " | Severity: " + violation.getSeverity());
        System.out.println("    Affected Dept: " + violation.getAffectedDepartment());
        System.out.println("    Escalation Status: Flagged for executive review");
    }

    @Override
    public void onViolationStatusChanged(ComplianceViolation violation) {
        if (violation.getSeverity() == ComplianceViolation.Severity.CRITICAL) {
            System.out.println("  [EXECUTIVE] CRITICAL VIOLATION UPDATE: " + violation.getStatus());
            System.out.println("    Violation ID: " + violation.getId());
        }
    }

    @Override
    public String getObserverName() {
        return departmentName;
    }

    public int getViolationsReceived() {
        return violationsReceived;
    }
}
