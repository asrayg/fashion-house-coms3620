package org.example.model;

/**
 * Predictive record attached to a MarketTrend describing the analyst's
 * estimate of when the trend will peak and decline. Generated on demand
 * from a trend's lifecycle stage and confidence.
 *
 * CSV format: id,trendId,projectedPeakSeason,projectedDeclineSeason,
 *             expectedDemandLift,recommendation,createdDate
 */
public class TrendForecast {

    private int    id;
    private int    trendId;
    private String projectedPeakSeason;
    private String projectedDeclineSeason;
    private double expectedDemandLift; // % expected lift in demand
    private String recommendation;
    private String createdDate;

    public TrendForecast(int id, int trendId, String projectedPeakSeason,
                         String projectedDeclineSeason, double expectedDemandLift,
                         String recommendation, String createdDate) {
        this.id                     = id;
        this.trendId                = trendId;
        this.projectedPeakSeason    = projectedPeakSeason;
        this.projectedDeclineSeason = projectedDeclineSeason;
        this.expectedDemandLift     = expectedDemandLift;
        this.recommendation         = recommendation;
        this.createdDate            = createdDate;
    }

    public static TrendForecast fromCSV(String line) {
        String[] p = line.split(",", 7);
        return new TrendForecast(
            Integer.parseInt(p[0].trim()),
            Integer.parseInt(p[1].trim()),
            p[2].trim(),
            p[3].trim(),
            Double.parseDouble(p[4].trim()),
            p[5].trim(),
            p[6].trim()
        );
    }

    public String toCSV() {
        return id + "," + trendId + "," + safe(projectedPeakSeason) + ","
             + safe(projectedDeclineSeason) + "," + expectedDemandLift + ","
             + safe(recommendation) + "," + createdDate;
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace(',', ';');
    }

    @Override
    public String toString() {
        return "[" + id + "] Trend #" + trendId
             + " | Peak: " + projectedPeakSeason
             + " | Decline: " + projectedDeclineSeason
             + " | Demand lift: " + String.format("%.1f", expectedDemandLift) + "%"
             + " | " + recommendation
             + " | Created: " + createdDate;
    }

    public int    getId()                     { return id; }
    public int    getTrendId()                { return trendId; }
    public String getProjectedPeakSeason()    { return projectedPeakSeason; }
    public String getProjectedDeclineSeason() { return projectedDeclineSeason; }
    public double getExpectedDemandLift()     { return expectedDemandLift; }
    public String getRecommendation()         { return recommendation; }
    public String getCreatedDate()            { return createdDate; }
}
