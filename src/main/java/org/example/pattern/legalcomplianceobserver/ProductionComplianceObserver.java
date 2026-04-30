package org.example.pattern.legalcomplianceobserver;

import org.example.model.ComplianceViolation;

/**
 * Production Department Observer — receives compliance notifications about production violations.
 */
public class ProductionComplianceObserver implements ComplianceObserver {
    private String departmentName = "Production Department";
    private int violationsReceived = 0;

    @Override
    public void onViolationRecorded(ComplianceViolation violation) {
        if ("Production".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "Production Department".equalsIgnoreCase(violation.getAffectedDepartment())) {
            violationsReceived++;
            System.out.println("  [PRODUCTION] Received violation notification: " + violation.getViolationType());
            System.out.println("    Description: " + violation.getDescription());
            System.out.println("    Severity: " + violation.getSeverity());
            System.out.println("    Action Required: Review production protocols by " + violation.getRemediationDueDate());
        }
    }

    @Override
    public void onViolationStatusChanged(ComplianceViolation violation) {
        if ("Production".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "Production Department".equalsIgnoreCase(violation.getAffectedDepartment())) {
            System.out.println("  [PRODUCTION] Violation status update: " + violation.getStatus());
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
