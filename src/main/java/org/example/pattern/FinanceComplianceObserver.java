package org.example.pattern;

import org.example.model.ComplianceViolation;

/**
 * HR/Finance Department Observer — receives compliance notifications about HR and financial violations.
 */
public class FinanceComplianceObserver implements ComplianceObserver {
    private String departmentName = "Finance Department";
    private int violationsReceived = 0;

    @Override
    public void onViolationRecorded(ComplianceViolation violation) {
        if ("Finance".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "Finance Department".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "HR".equalsIgnoreCase(violation.getAffectedDepartment())) {
            violationsReceived++;
            System.out.println("  [FINANCE] Received violation notification: " + violation.getViolationType());
            System.out.println("    Description: " + violation.getDescription());
            System.out.println("    Severity: " + violation.getSeverity());
            System.out.println("    Required Remediation by: " + violation.getRemediationDueDate());
        }
    }

    @Override
    public void onViolationStatusChanged(ComplianceViolation violation) {
        if ("Finance".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "Finance Department".equalsIgnoreCase(violation.getAffectedDepartment()) ||
            "HR".equalsIgnoreCase(violation.getAffectedDepartment())) {
            System.out.println("  [FINANCE] Violation status update: " + violation.getStatus());
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
