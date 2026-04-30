package org.example.model;

/**
 * Represents a compliance violation found during audits or routine monitoring.
 * Used with Observer pattern to notify departments of violations.
 *
 * CSV format: id,auditId,violationType,description,severity,affectedDepartment,
 *             status,remediationDueDate,remediationCompletionDate,notes,createdDate
 */
public class ComplianceViolation {

    public enum Status { REPORTED, UNDER_REVIEW, REMEDIATION_IN_PROGRESS, REMEDIATED, CLOSED, DISPUTED }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private int id;
    private int auditId;
    private String violationType;
    private String description;
    private Severity severity;
    private String affectedDepartment;
    private Status status;
    private String remediationDueDate;
    private String remediationCompletionDate;
    private String notes;
    private String createdDate;

    public ComplianceViolation(int id, int auditId, String violationType, String description,
                              Severity severity, String affectedDepartment, Status status,
                              String remediationDueDate, String remediationCompletionDate,
                              String notes, String createdDate) {
        this.id = id;
        this.auditId = auditId;
        this.violationType = violationType;
        this.description = description;
        this.severity = severity;
        this.affectedDepartment = affectedDepartment;
        this.status = status;
        this.remediationDueDate = remediationDueDate;
        this.remediationCompletionDate = remediationCompletionDate;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    public String toCSV() {
        return id + "," + auditId + "," + violationType + "," + description + "," + severity.name() + ","
             + affectedDepartment + "," + status.name() + "," + remediationDueDate + ","
             + remediationCompletionDate + "," + notes + "," + createdDate;
    }

    public static ComplianceViolation fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new ComplianceViolation(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            parts[2],
            parts[3],
            Severity.valueOf(parts[4]),
            parts[5],
            Status.valueOf(parts[6]),
            parts[7],
            parts[8],
            parts[9],
            parts[10]
        );
    }

    @Override
    public String toString() {
        return String.format("[ID: %d | Audit: %d] %s Violation | Dept: %s | Severity: %s | Status: %s",
            id, auditId, violationType, affectedDepartment, severity, status);
    }

    // Getters
    public int getId() { return id; }
    public int getAuditId() { return auditId; }
    public String getViolationType() { return violationType; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public String getAffectedDepartment() { return affectedDepartment; }
    public Status getStatus() { return status; }
    public String getRemediationDueDate() { return remediationDueDate; }
    public String getRemediationCompletionDate() { return remediationCompletionDate; }
    public String getNotes() { return notes; }
    public String getCreatedDate() { return createdDate; }

    // Setters
    public void setStatus(Status status) { this.status = status; }
    public void setRemediationCompletionDate(String date) { this.remediationCompletionDate = date; }
    public void setNotes(String notes) { this.notes = notes; }
}
