package org.example.model;

/**
 * Represents an advertising campaign linked to a collection.
 * Supports multi-platform targeting, per-platform budget allocation,
 * lifecycle status management, and performance metrics tracking.
 *
 * CSV format:
 * id,collectionId,name,platforms(|-sep),platformBudgets(|-sep),
 * totalBudget,startDate,endDate,status,targetImpressions,targetConversions,
 * actualImpressions,actualConversions,notes
 *
 * Backward compatibility:
 * Older CSV rows with 8 fields are supported:
 * id,collectionId,name,platforms,platformBudgets,totalBudget,startDate,status
 */
public class AdCampaign {

    public enum Status { PLANNED, ACTIVE, PAUSED, COMPLETED, CANCELLED }

    private int    id;
    private int    collectionId;
    private String name;
    private String platforms;           
    private String platformBudgets;     
    private double totalBudget;
    private String startDate;
    private String endDate;
    private Status status;
    private int    targetImpressions;
    private int    targetConversions;
    private int    actualImpressions;
    private int    actualConversions;
    private String notes;

    public AdCampaign(int id, int collectionId, String name,
                      String platforms, String platformBudgets,
                      double totalBudget, String startDate, String endDate,
                      Status status, int targetImpressions, int targetConversions,
                      int actualImpressions, int actualConversions, String notes) {
        this.id                 = id;
        this.collectionId       = collectionId;
        this.name               = name;
        this.platforms          = platforms;
        this.platformBudgets    = platformBudgets;
        this.totalBudget        = totalBudget;
        this.startDate          = startDate;
        this.endDate            = endDate;
        this.status             = status;
        this.targetImpressions  = targetImpressions;
        this.targetConversions  = targetConversions;
        this.actualImpressions  = actualImpressions;
        this.actualConversions  = actualConversions;
        this.notes              = notes == null ? "" : notes;
    }

    public String toCSV() {
        return id + "," + collectionId + "," + name + ","
                + platforms + "," + platformBudgets + ","
                + totalBudget + "," + startDate + "," + endDate + ","
                + status.name() + "," + targetImpressions + ","
                + targetConversions + "," + actualImpressions + ","
                + actualConversions + "," + notes;
    }

    public static AdCampaign fromCSV(String line) {
        // New format: 14 fields (limit keeps notes intact even if it contains commas later)
        String[] p = line.split(",", 14);
        if (p.length >= 14) {
            return new AdCampaign(
                    Integer.parseInt(p[0].trim()),
                    Integer.parseInt(p[1].trim()),
                    p[2].trim(),
                    p[3].trim(),
                    p[4].trim(),
                    Double.parseDouble(p[5].trim()),
                    p[6].trim(),
                    p[7].trim(),
                    Status.valueOf(p[8].trim()),
                    Integer.parseInt(p[9].trim()),
                    Integer.parseInt(p[10].trim()),
                    Integer.parseInt(p[11].trim()),
                    Integer.parseInt(p[12].trim()),
                    p[13].trim()
            );
        }

        // Backward-compatible old format: 8 fields
        String[] old = line.split(",", 8);
        if (old.length == 8) {
            Status s;
            try {
                s = Status.valueOf(old[7].trim());
            } catch (IllegalArgumentException e) {
                s = Status.PLANNED;
            }
            String start = old[6].trim();
            return new AdCampaign(
                    Integer.parseInt(old[0].trim()),
                    Integer.parseInt(old[1].trim()),
                    old[2].trim(),
                    old[3].trim(),
                    old[4].trim(),
                    Double.parseDouble(old[5].trim()),
                    start,
                    start,
                    s,
                    0, 0,
                    0, 0,
                    ""
            );
        }

        throw new IllegalArgumentException("Invalid campaign CSV row: " + line);
    }

    /** Conversion rate as a percentage of impressions. */
    public double getConversionRate() {
        if (actualImpressions == 0) return 0.0;
        return (actualConversions * 100.0) / actualImpressions;
    }

    /** Budget spent per actual conversion. Returns -1 if no conversions yet. */
    public double getCostPerConversion() {
        if (actualConversions == 0) return -1;
        return totalBudget / actualConversions;
    }

    /** How far through the impression target we are (0-100). */
    public double getImpressionProgress() {
        if (targetImpressions == 0) return 0.0;
        return Math.min(100.0, (actualImpressions * 100.0) / targetImpressions);
    }

    @Override
    public String toString() {
        String[] pList = platforms.split("\\|");
        String[] bList = platformBudgets.split("\\|");
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(id).append("] ").append(name)
          .append(" | Collection: ").append(collectionId)
          .append(" | Status: ").append(status)
          .append(" | ").append(startDate).append(" → ").append(endDate)
          .append(" | Total Budget: $").append(String.format("%.2f", totalBudget))
          .append("\n  Platforms:");
        for (int i = 0; i < pList.length; i++) {
            sb.append("\n    - ").append(pList[i].trim())
              .append(": $").append(i < bList.length ? bList[i].trim() : "0.00");
        }
        sb.append("\n  Targets: ").append(targetImpressions).append(" impressions | ")
          .append(targetConversions).append(" conversions");
        if (actualImpressions > 0 || actualConversions > 0) {
            sb.append("\n  Actuals: ").append(actualImpressions).append(" impressions | ")
              .append(actualConversions).append(" conversions")
              .append(" | Conv. Rate: ").append(String.format("%.1f", getConversionRate())).append("%");
        }
        if (!notes.isEmpty()) sb.append("\n  Notes: ").append(notes);
        return sb.toString();
    }

    // --- Getters ---
    public int    getId()                  { return id; }
    public int    getCollectionId()        { return collectionId; }
    public String getName()                { return name; }
    public String getPlatforms()           { return platforms; }
    public String getPlatformBudgets()     { return platformBudgets; }
    public double getTotalBudget()         { return totalBudget; }
    public String getStartDate()           { return startDate; }
    public String getEndDate()             { return endDate; }
    public Status getStatus()              { return status; }
    public int    getTargetImpressions()   { return targetImpressions; }
    public int    getTargetConversions()   { return targetConversions; }
    public int    getActualImpressions()   { return actualImpressions; }
    public int    getActualConversions()   { return actualConversions; }
    public String getNotes()               { return notes; }

    // --- Setters ---
    public void setStatus(Status status)                     { this.status = status; }
    public void setActualImpressions(int actualImpressions)  { this.actualImpressions = actualImpressions; }
    public void setActualConversions(int actualConversions)  { this.actualConversions = actualConversions; }
    public void setNotes(String notes)                       { this.notes = notes == null ? "" : notes; }
    public void setPlatformBudgets(String platformBudgets)   { this.platformBudgets = platformBudgets; }
    public void setTotalBudget(double totalBudget)           { this.totalBudget = totalBudget; }
    public void setPlatforms(String platforms)               { this.platforms = platforms; }
}
