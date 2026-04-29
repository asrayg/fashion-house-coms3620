package org.example.model;

/**
 * Multi-criteria compliance + commercial evaluation of a wholesale partner.
 * Drives the onboarding workflow: PENDING → IN_REVIEW → APPROVED / REJECTED / REVISION_NEEDED.
 *
 * CSV format: id,partnerId,departmentId,reviewerName,
 *             financialStability,brandAlignment,salesPotential,distributionReach,paymentReliability,
 *             overallScore,status,feedback,evaluationDate
 */
public class PartnerEvaluation {

    public enum Status { PENDING, IN_REVIEW, APPROVED, REJECTED, REVISION_NEEDED }

    private int id;
    private int partnerId;
    private int departmentId;
    private String reviewerName;
    private double financialStabilityScore;
    private double brandAlignmentScore;
    private double salesPotentialScore;
    private double distributionReachScore;
    private double paymentReliabilityScore;
    private double overallScore;
    private Status status;
    private String feedback;
    private String evaluationDate;

    public PartnerEvaluation(int id, int partnerId, int departmentId, String reviewerName,
                             double financialStabilityScore, double brandAlignmentScore,
                             double salesPotentialScore, double distributionReachScore,
                             double paymentReliabilityScore, double overallScore,
                             Status status, String feedback, String evaluationDate) {
        this.id = id;
        this.partnerId = partnerId;
        this.departmentId = departmentId;
        this.reviewerName = reviewerName;
        this.financialStabilityScore = financialStabilityScore;
        this.brandAlignmentScore = brandAlignmentScore;
        this.salesPotentialScore = salesPotentialScore;
        this.distributionReachScore = distributionReachScore;
        this.paymentReliabilityScore = paymentReliabilityScore;
        this.overallScore = overallScore;
        this.status = status;
        this.feedback = feedback;
        this.evaluationDate = evaluationDate;
    }

    public String toCSV() {
        return id + "," + partnerId + "," + departmentId + "," + reviewerName
             + "," + financialStabilityScore + "," + brandAlignmentScore
             + "," + salesPotentialScore + "," + distributionReachScore
             + "," + paymentReliabilityScore + "," + overallScore
             + "," + status.name() + "," + feedback + "," + evaluationDate;
    }

    public static PartnerEvaluation fromCSV(String line) {
        String[] parts = line.split(",", 13);
        return new PartnerEvaluation(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            Integer.parseInt(parts[2].trim()),
            parts[3].trim(),
            Double.parseDouble(parts[4].trim()),
            Double.parseDouble(parts[5].trim()),
            Double.parseDouble(parts[6].trim()),
            Double.parseDouble(parts[7].trim()),
            Double.parseDouble(parts[8].trim()),
            Double.parseDouble(parts[9].trim()),
            Status.valueOf(parts[10].trim()),
            parts[11].trim(),
            parts[12].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] PartnerID: " + partnerId + " | Dept: " + departmentId
             + " | Reviewer: " + reviewerName
             + " | Scores [F:" + financialStabilityScore + " B:" + brandAlignmentScore
             + " S:" + salesPotentialScore + " D:" + distributionReachScore
             + " P:" + paymentReliabilityScore + "]"
             + " | Overall: " + String.format("%.1f", overallScore)
             + " | Status: " + status + " | Date: " + evaluationDate;
    }

    public int getId()                          { return id; }
    public int getPartnerId()                   { return partnerId; }
    public int getDepartmentId()                { return departmentId; }
    public String getReviewerName()             { return reviewerName; }
    public double getFinancialStabilityScore()  { return financialStabilityScore; }
    public double getBrandAlignmentScore()      { return brandAlignmentScore; }
    public double getSalesPotentialScore()      { return salesPotentialScore; }
    public double getDistributionReachScore()   { return distributionReachScore; }
    public double getPaymentReliabilityScore()  { return paymentReliabilityScore; }
    public double getOverallScore()             { return overallScore; }
    public Status getStatus()                   { return status; }
    public String getFeedback()                 { return feedback; }
    public String getEvaluationDate()           { return evaluationDate; }
}
