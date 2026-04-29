package org.example.model;

/**
 * Tracks competitor brand activity that informs trend analysis.
 * Independent of MarketTrend but often referenced as supporting evidence.
 *
 * CSV format: id,competitorName,activityType,description,observedDate,impactLevel
 *
 * impactLevel: LOW, MEDIUM, HIGH
 */
public class CompetitorObservation {

    public enum ImpactLevel { LOW, MEDIUM, HIGH }

    private int         id;
    private String      competitorName;
    private String      activityType;   // e.g. "ProductLaunch", "PriceCut", "MarketingShift"
    private String      description;
    private String      observedDate;
    private ImpactLevel impactLevel;

    public CompetitorObservation(int id, String competitorName, String activityType,
                                 String description, String observedDate, ImpactLevel impactLevel) {
        this.id             = id;
        this.competitorName = competitorName;
        this.activityType   = activityType;
        this.description    = description;
        this.observedDate   = observedDate;
        this.impactLevel    = impactLevel;
    }

    public static CompetitorObservation fromCSV(String line) {
        String[] p = line.split(",", 6);
        return new CompetitorObservation(
            Integer.parseInt(p[0].trim()),
            p[1].trim(),
            p[2].trim(),
            p[3].trim(),
            p[4].trim(),
            ImpactLevel.valueOf(p[5].trim())
        );
    }

    public String toCSV() {
        return id + "," + safe(competitorName) + "," + safe(activityType) + ","
             + safe(description) + "," + observedDate + "," + impactLevel;
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace(',', ';');
    }

    @Override
    public String toString() {
        return "[" + id + "] " + competitorName
             + " | " + activityType
             + " | Impact: " + impactLevel
             + " | Observed: " + observedDate
             + "\n  " + description;
    }

    public int         getId()             { return id; }
    public String      getCompetitorName() { return competitorName; }
    public String      getActivityType()   { return activityType; }
    public String      getDescription()    { return description; }
    public String      getObservedDate()   { return observedDate; }
    public ImpactLevel getImpactLevel()    { return impactLevel; }
}
