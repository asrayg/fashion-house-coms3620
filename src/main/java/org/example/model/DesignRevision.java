package org.example.model;

/**
 * Tracks revision history for garment designs that go through the review cycle.
 * Each revision is linked to the review that triggered it.
 *
 * CSV format: id,garmentDesignId,revisionNumber,changeDescription,revisedBy,revisionDate,previousReviewId,budgetImpact
 */
public class DesignRevision {

    private int id;
    private int garmentDesignId;
    private int revisionNumber;
    private String changeDescription;
    private String revisedBy;
    private String revisionDate;
    private int previousReviewId;
    private double budgetImpact;

    public DesignRevision(int id, int garmentDesignId, int revisionNumber,
                          String changeDescription, String revisedBy, String revisionDate,
                          int previousReviewId, double budgetImpact) {
        this.id = id;
        this.garmentDesignId = garmentDesignId;
        this.revisionNumber = revisionNumber;
        this.changeDescription = changeDescription;
        this.revisedBy = revisedBy;
        this.revisionDate = revisionDate;
        this.previousReviewId = previousReviewId;
        this.budgetImpact = budgetImpact;
    }

    public String toCSV() {
        return id + "," + garmentDesignId + "," + revisionNumber + "," + changeDescription
             + "," + revisedBy + "," + revisionDate + "," + previousReviewId + "," + budgetImpact;
    }

    public static DesignRevision fromCSV(String line) {
        String[] parts = line.split(",", 8);
        return new DesignRevision(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim()),
            parts[3].trim(),
            parts[4].trim(),
            parts[5].trim(),
            Integer.parseInt(parts[6].trim()),
            Double.parseDouble(parts[7].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] GarmentID: " + garmentDesignId + " | Rev #" + revisionNumber
             + " | By: " + revisedBy + " | Date: " + revisionDate
             + " | ReviewID: " + previousReviewId
             + " | Budget Impact: $" + String.format("%.2f", budgetImpact)
             + " | Changes: " + changeDescription;
    }

    // --- Getters ---
    public int getId()                   { return id; }
    public int getGarmentDesignId()      { return garmentDesignId; }
    public int getRevisionNumber()       { return revisionNumber; }
    public String getChangeDescription() { return changeDescription; }
    public String getRevisedBy()         { return revisedBy; }
    public String getRevisionDate()      { return revisionDate; }
    public int getPreviousReviewId()     { return previousReviewId; }
    public double getBudgetImpact()      { return budgetImpact; }
}
