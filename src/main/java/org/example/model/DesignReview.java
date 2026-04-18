package org.example.model;

/**
 * Represents a formal design review for a garment design, scored on multiple criteria.
 * Part of the multi-stage design approval workflow.
 *
 * CSV format: id,garmentDesignId,departmentId,reviewerName,creativityScore,feasibilityScore,
 *             marketFitScore,costEfficiencyScore,brandAlignmentScore,overallScore,
 *             status,feedback,reviewDate
 */
public class DesignReview {

    public enum Status { PENDING, IN_REVIEW, APPROVED, REJECTED, REVISION_NEEDED }

    private int id;
    private int garmentDesignId;
    private int departmentId;
    private String reviewerName;
    private double creativityScore;
    private double feasibilityScore;
    private double marketFitScore;
    private double costEfficiencyScore;
    private double brandAlignmentScore;
    private double overallScore;
    private Status status;
    private String feedback;
    private String reviewDate;

    public DesignReview(int id, int garmentDesignId, int departmentId, String reviewerName,
                        double creativityScore, double feasibilityScore, double marketFitScore,
                        double costEfficiencyScore, double brandAlignmentScore, double overallScore,
                        Status status, String feedback, String reviewDate) {
        this.id = id;
        this.garmentDesignId = garmentDesignId;
        this.departmentId = departmentId;
        this.reviewerName = reviewerName;
        this.creativityScore = creativityScore;
        this.feasibilityScore = feasibilityScore;
        this.marketFitScore = marketFitScore;
        this.costEfficiencyScore = costEfficiencyScore;
        this.brandAlignmentScore = brandAlignmentScore;
        this.overallScore = overallScore;
        this.status = status;
        this.feedback = feedback;
        this.reviewDate = reviewDate;
    }

    public String toCSV() {
        return id + "," + garmentDesignId + "," + departmentId + "," + reviewerName
             + "," + creativityScore + "," + feasibilityScore + "," + marketFitScore
             + "," + costEfficiencyScore + "," + brandAlignmentScore + "," + overallScore
             + "," + status.name() + "," + feedback + "," + reviewDate;
    }

    public static DesignReview fromCSV(String line) {
        String[] parts = line.split(",", 13);
        return new DesignReview(
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
        return "[" + id + "] GarmentID: " + garmentDesignId + " | Dept: " + departmentId
             + " | Reviewer: " + reviewerName
             + " | Scores [C:" + creativityScore + " F:" + feasibilityScore
             + " M:" + marketFitScore + " $:" + costEfficiencyScore
             + " B:" + brandAlignmentScore + "]"
             + " | Overall: " + String.format("%.1f", overallScore)
             + " | Status: " + status + " | Date: " + reviewDate;
    }

    // --- Getters ---
    public int getId()                    { return id; }
    public int getGarmentDesignId()       { return garmentDesignId; }
    public int getDepartmentId()          { return departmentId; }
    public String getReviewerName()       { return reviewerName; }
    public double getCreativityScore()    { return creativityScore; }
    public double getFeasibilityScore()   { return feasibilityScore; }
    public double getMarketFitScore()     { return marketFitScore; }
    public double getCostEfficiencyScore() { return costEfficiencyScore; }
    public double getBrandAlignmentScore() { return brandAlignmentScore; }
    public double getOverallScore()       { return overallScore; }
    public Status getStatus()             { return status; }
    public String getFeedback()           { return feedback; }
    public String getReviewDate()         { return reviewDate; }

    // --- Setters ---
    public void setStatus(Status status)          { this.status = status; }
    public void setFeedback(String feedback)      { this.feedback = feedback; }
    public void setOverallScore(double score)     { this.overallScore = score; }
}
