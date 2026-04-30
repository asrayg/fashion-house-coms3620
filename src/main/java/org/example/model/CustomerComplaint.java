package org.example.model;

/**
 * A complaint filed by or on behalf of a registered customer.
 *
 * CSV format (7 fields, split(",", 7)):
 *   id,profileId,description,category,severity,status,loggedDate
 *
 * Note: commas in 'description' are sanitized to semicolons before saving.
 */
public class CustomerComplaint {

    public enum Category { PRODUCT_QUALITY, DELIVERY, BILLING, SERVICE, OTHER }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status   { OPEN, ESCALATED, RESOLVED }

    private final int id;
    private final int profileId;
    private final String description;
    private final Category category;
    private final Severity severity;
    private Status status;
    private final String loggedDate;

    public CustomerComplaint(int id, int profileId, String description,
                             Category category, Severity severity,
                             Status status, String loggedDate) {
        this.id = id;
        this.profileId = profileId;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.status = status;
        this.loggedDate = loggedDate;
    }

    public String toCSV() {
        return id + "," + profileId + "," + description + "," + category.name() + ","
             + severity.name() + "," + status.name() + "," + loggedDate;
    }

    public static CustomerComplaint fromCSV(String line) {
        String[] p = line.split(",", 7);
        return new CustomerComplaint(
            Integer.parseInt(p[0].trim()),
            Integer.parseInt(p[1].trim()),
            p[2].trim(),
            Category.valueOf(p[3].trim()),
            Severity.valueOf(p[4].trim()),
            Status.valueOf(p[5].trim()),
            p[6].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] Profile #" + profileId
             + " | Category: " + category
             + " | Severity: " + severity
             + " | Status: " + status
             + " | Filed: " + loggedDate
             + " | " + description;
    }

    public int getId()             { return id; }
    public int getProfileId()      { return profileId; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }
    public Severity getSeverity()  { return severity; }
    public Status getStatus()      { return status; }
    public String getLoggedDate()  { return loggedDate; }

    public void setStatus(Status status) { this.status = status; }
}
