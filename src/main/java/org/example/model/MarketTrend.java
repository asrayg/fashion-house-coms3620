package org.example.model;

/**
 * Represents a market or fashion trend logged by the Research & Trend Analysis department.
 * Acts as the central domain entity for the Observer notification flow:
 * once persisted, every registered TrendObserver is notified so dependent
 * departments (Design, Marketing, Production) can react.
 *
 * CSV format (12 fields):
 *   id,category,name,description,source,confidenceLevel,season,
 *   targetRegion,lifecycleStage,relatedCollectionId,dateLogged,status
 *
 * Notes:
 *  - relatedCollectionId = -1 means "not linked to a specific collection"
 *  - confidenceLevel is bounded to [1, 5]
 */
public class MarketTrend {

    public enum Category       { FASHION, COLOR, FABRIC, SILHOUETTE, CONSUMER_BEHAVIOR }
    public enum LifecycleStage { EMERGING, RISING, PEAK, DECLINING, FADED }
    public enum Status         { ACTIVE, ARCHIVED }

    private int            id;
    private Category       category;
    private String         name;
    private String         description;
    private String         source;
    private int            confidenceLevel;     // 1..5
    private String         season;              // e.g. "Spring2026"
    private String         targetRegion;        // e.g. "Global", "North America"
    private LifecycleStage lifecycleStage;
    private int            relatedCollectionId; // -1 = none
    private String         dateLogged;          // ISO yyyy-MM-dd
    private Status         status;

    public MarketTrend(int id, Category category, String name, String description,
                       String source, int confidenceLevel, String season,
                       String targetRegion, LifecycleStage lifecycleStage,
                       int relatedCollectionId, String dateLogged, Status status) {
        this.id                  = id;
        this.category            = category;
        this.name                = name;
        this.description         = description;
        this.source              = source;
        this.confidenceLevel     = confidenceLevel;
        this.season              = season;
        this.targetRegion        = targetRegion;
        this.lifecycleStage      = lifecycleStage;
        this.relatedCollectionId = relatedCollectionId;
        this.dateLogged          = dateLogged;
        this.status              = status;
    }

    public static MarketTrend fromCSV(String line) {
        String[] p = line.split(",", 12);
        if (p.length >= 12) {
            return new MarketTrend(
                Integer.parseInt(p[0].trim()),
                Category.valueOf(p[1].trim()),
                p[2].trim(),
                p[3].trim(),
                p[4].trim(),
                Integer.parseInt(p[5].trim()),
                p[6].trim(),
                p[7].trim(),
                LifecycleStage.valueOf(p[8].trim()),
                Integer.parseInt(p[9].trim()),
                p[10].trim(),
                Status.valueOf(p[11].trim())
            );
        }
        // Backward-compat for early 9-field rows from initial implementation:
        // id,category,name,description,source,confidence,season,dateLogged,status
        String[] old = line.split(",", 9);
        if (old.length == 9) {
            return new MarketTrend(
                Integer.parseInt(old[0].trim()),
                Category.valueOf(old[1].trim()),
                old[2].trim(),
                old[3].trim(),
                old[4].trim(),
                Integer.parseInt(old[5].trim()),
                old[6].trim(),
                "Global",
                LifecycleStage.EMERGING,
                -1,
                old[7].trim(),
                Status.valueOf(old[8].trim())
            );
        }
        throw new IllegalArgumentException("Invalid MarketTrend CSV row: " + line);
    }

    public String toCSV() {
        return id + "," + category + "," + safe(name) + "," + safe(description) + ","
             + safe(source) + "," + confidenceLevel + "," + safe(season) + ","
             + safe(targetRegion) + "," + lifecycleStage + "," + relatedCollectionId + ","
             + dateLogged + "," + status;
    }

    /** Strip commas from free-text so they don't break CSV row layout. */
    private static String safe(String s) {
        return s == null ? "" : s.replace(',', ';');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(id).append("] ").append(name)
          .append(" | ").append(category)
          .append(" | Lifecycle: ").append(lifecycleStage)
          .append(" | Season: ").append(season)
          .append(" | Region: ").append(targetRegion)
          .append(" | Confidence: ").append(confidenceLevel).append("/5")
          .append("\n  Source: ").append(source)
          .append(" | Logged: ").append(dateLogged)
          .append(" | Status: ").append(status);
        if (relatedCollectionId > 0) {
            sb.append(" | Linked Collection: #").append(relatedCollectionId);
        }
        if (description != null && !description.isEmpty()) {
            sb.append("\n  ").append(description);
        }
        return sb.toString();
    }

    // --- Getters ---
    public int            getId()                  { return id; }
    public Category       getCategory()            { return category; }
    public String         getName()                { return name; }
    public String         getDescription()         { return description; }
    public String         getSource()              { return source; }
    public int            getConfidenceLevel()     { return confidenceLevel; }
    public String         getSeason()              { return season; }
    public String         getTargetRegion()        { return targetRegion; }
    public LifecycleStage getLifecycleStage()      { return lifecycleStage; }
    public int            getRelatedCollectionId() { return relatedCollectionId; }
    public String         getDateLogged()          { return dateLogged; }
    public Status         getStatus()              { return status; }

    // --- Setters ---
    public void setStatus(Status status)                  { this.status = status; }
    public void setLifecycleStage(LifecycleStage stage)   { this.lifecycleStage = stage; }
    public void setConfidenceLevel(int confidenceLevel)   { this.confidenceLevel = confidenceLevel; }
    public void setDescription(String description)        { this.description = description; }
}
