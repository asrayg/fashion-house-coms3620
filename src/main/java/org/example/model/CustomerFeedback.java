package org.example.model;

/**
 * Post-resolution feedback submitted by a customer for a resolved complaint.
 *
 * CSV format (7 fields, split(",", 7)):
 *   id,complaintId,profileId,feedbackText,satisfactionScore,issueConfirmed,feedbackDate
 *
 * Note: commas in 'feedbackText' are sanitized to semicolons before saving.
 */
public class CustomerFeedback {

    private final int id;
    private final int complaintId;
    private final int profileId;
    private final String feedbackText;
    private final int satisfactionScore;
    private final boolean issueConfirmed;
    private final String feedbackDate;

    public CustomerFeedback(int id, int complaintId, int profileId,
                            String feedbackText, int satisfactionScore,
                            boolean issueConfirmed, String feedbackDate) {
        this.id = id;
        this.complaintId = complaintId;
        this.profileId = profileId;
        this.feedbackText = feedbackText;
        this.satisfactionScore = satisfactionScore;
        this.issueConfirmed = issueConfirmed;
        this.feedbackDate = feedbackDate;
    }

    public String toCSV() {
        return id + "," + complaintId + "," + profileId + "," + feedbackText + ","
             + satisfactionScore + "," + issueConfirmed + "," + feedbackDate;
    }

    public static CustomerFeedback fromCSV(String line) {
        String[] p = line.split(",", 7);
        return new CustomerFeedback(
            Integer.parseInt(p[0].trim()),
            Integer.parseInt(p[1].trim()),
            Integer.parseInt(p[2].trim()),
            p[3].trim(),
            Integer.parseInt(p[4].trim()),
            Boolean.parseBoolean(p[5].trim()),
            p[6].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] Complaint #" + complaintId
             + " | Score: " + satisfactionScore + "/5"
             + " | Confirmed: " + issueConfirmed
             + " | Date: " + feedbackDate
             + " | " + feedbackText;
    }

    public int getId()               { return id; }
    public int getComplaintId()      { return complaintId; }
    public int getProfileId()        { return profileId; }
    public String getFeedbackText()  { return feedbackText; }
    public int getSatisfactionScore(){ return satisfactionScore; }
    public boolean isIssueConfirmed(){ return issueConfirmed; }
    public String getFeedbackDate()  { return feedbackDate; }
}
