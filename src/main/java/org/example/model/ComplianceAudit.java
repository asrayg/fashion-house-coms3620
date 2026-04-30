package org.example.model;

/**
 * Represents a compliance audit record for legal and regulatory monitoring.
 * UC: Record Compliance Audit (Iteration 3 — Legal & Compliance)
 *
 * CSV format: id,auditDate,auditType,department,findings,status,severity,
 *             assignedTo,dueDate,completionDate,notes,createdDate
 */
public class ComplianceAudit {

    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum AuditType { LABOR_STANDARDS, ENVIRONMENTAL, SAFETY, DATA_PRIVACY, FINANCIAL, SUPPLIER, OTHER }

    private int id;
    private String auditDate;
    private AuditType auditType;
    private String department;
    private String findings;
    private Status status;
    private Severity severity;
    private String assignedTo;
    private String dueDate;
    private String completionDate;
    private String notes;
    private String createdDate;

    public ComplianceAudit(int id, String auditDate, AuditType auditType, String department,
                          String findings, Status status, Severity severity, String assignedTo,
                          String dueDate, String completionDate, String notes, String createdDate) {
        this.id = id;
        this.auditDate = auditDate;
        this.auditType = auditType;
        this.department = department;
        this.findings = findings;
        this.status = status;
        this.severity = severity;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
        this.completionDate = completionDate;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    public String toCSV() {
        return id + "," + auditDate + "," + auditType.name() + "," + department + "," + findings + ","
             + status.name() + "," + severity.name() + "," + assignedTo + "," + dueDate + ","
             + completionDate + "," + notes + "," + createdDate;
    }

    public static ComplianceAudit fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new ComplianceAudit(
            Integer.parseInt(parts[0]),
            parts[1],
            AuditType.valueOf(parts[2]),
            parts[3],
            parts[4],
            Status.valueOf(parts[5]),
            Severity.valueOf(parts[6]),
            parts[7],
            parts[8],
            parts[9],
            parts[10],
            parts[11]
        );
    }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s Audit | Dept: %s | Status: %s | Severity: %s | Type: %s | Findings: %s",
            id, auditType, department, status, severity, auditType, findings);
    }

    // Getters
    public int getId() { return id; }
    public String getAuditDate() { return auditDate; }
    public AuditType getAuditType() { return auditType; }
    public String getDepartment() { return department; }
    public String getFindings() { return findings; }
    public Status getStatus() { return status; }
    public Severity getSeverity() { return severity; }
    public String getAssignedTo() { return assignedTo; }
    public String getDueDate() { return dueDate; }
    public String getCompletionDate() { return completionDate; }
    public String getNotes() { return notes; }
    public String getCreatedDate() { return createdDate; }

    // Setters
    public void setStatus(Status status) { this.status = status; }
    public void setCompletionDate(String completionDate) { this.completionDate = completionDate; }
    public void setNotes(String notes) { this.notes = notes; }
}
