package org.example.model;

/**
 * Tracks contract / agreement adjustments applied to a partner after an evaluation.
 * Each adjustment is tied to the evaluation that triggered it (revision-style audit).
 *
 * CSV format: id,partnerId,adjustmentNumber,changeDescription,adjustedBy,adjustmentDate,
 *             previousEvaluationId,budgetImpact
 */
public class ContractAdjustment {

    private int id;
    private int partnerId;
    private int adjustmentNumber;
    private String changeDescription;
    private String adjustedBy;
    private String adjustmentDate;
    private int previousEvaluationId;
    private double budgetImpact;

    public ContractAdjustment(int id, int partnerId, int adjustmentNumber,
                              String changeDescription, String adjustedBy, String adjustmentDate,
                              int previousEvaluationId, double budgetImpact) {
        this.id = id;
        this.partnerId = partnerId;
        this.adjustmentNumber = adjustmentNumber;
        this.changeDescription = changeDescription;
        this.adjustedBy = adjustedBy;
        this.adjustmentDate = adjustmentDate;
        this.previousEvaluationId = previousEvaluationId;
        this.budgetImpact = budgetImpact;
    }

    public String toCSV() {
        return id + "," + partnerId + "," + adjustmentNumber + "," + changeDescription
             + "," + adjustedBy + "," + adjustmentDate + "," + previousEvaluationId
             + "," + budgetImpact;
    }

    public static ContractAdjustment fromCSV(String line) {
        String[] parts = line.split(",", 8);
        return new ContractAdjustment(
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
        return "[" + id + "] PartnerID: " + partnerId + " | Adj #" + adjustmentNumber
             + " | By: " + adjustedBy + " | Date: " + adjustmentDate
             + " | EvalID: " + previousEvaluationId
             + " | Budget Impact: $" + String.format("%.2f", budgetImpact)
             + " | Changes: " + changeDescription;
    }

    public int getId()                   { return id; }
    public int getPartnerId()            { return partnerId; }
    public int getAdjustmentNumber()     { return adjustmentNumber; }
    public String getChangeDescription() { return changeDescription; }
    public String getAdjustedBy()        { return adjustedBy; }
    public String getAdjustmentDate()    { return adjustmentDate; }
    public int getPreviousEvaluationId() { return previousEvaluationId; }
    public double getBudgetImpact()      { return budgetImpact; }
}
