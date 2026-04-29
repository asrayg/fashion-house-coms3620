package org.example.model;

/**
 * Represents a single notification fired by a TrendObserver when a
 * MarketTrend is logged. Persisted to data/research/trend_alerts.csv
 * so each department can audit which trends it was told about.
 *
 * CSV format: id,trendId,department,priority,alertDate,acknowledged,message
 */
public class TrendAlert {

    public enum Priority { LOW, MEDIUM, HIGH }

    private int      id;
    private int      trendId;
    private String   department;
    private Priority priority;
    private String   alertDate;
    private boolean  acknowledged;
    private String   message;

    public TrendAlert(int id, int trendId, String department, Priority priority,
                      String alertDate, boolean acknowledged, String message) {
        this.id           = id;
        this.trendId      = trendId;
        this.department   = department;
        this.priority     = priority;
        this.alertDate    = alertDate;
        this.acknowledged = acknowledged;
        this.message      = message;
    }

    public static TrendAlert fromCSV(String line) {
        // limit=7 keeps message intact even if it contains commas (it shouldn't, but defensive)
        String[] p = line.split(",", 7);
        if (p.length >= 7) {
            return new TrendAlert(
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                p[2].trim(),
                Priority.valueOf(p[3].trim()),
                p[4].trim(),
                Boolean.parseBoolean(p[5].trim()),
                p[6].trim()
            );
        }
        // Backward-compat for early 5-field rows: id,trendId,department,alertDate,message
        String[] old = line.split(",", 5);
        if (old.length == 5) {
            return new TrendAlert(
                Integer.parseInt(old[0].trim()),
                Integer.parseInt(old[1].trim()),
                old[2].trim(),
                Priority.MEDIUM,
                old[3].trim(),
                false,
                old[4].trim()
            );
        }
        throw new IllegalArgumentException("Invalid TrendAlert CSV row: " + line);
    }

    public String toCSV() {
        return id + "," + trendId + "," + safe(department) + "," + priority + ","
             + alertDate + "," + acknowledged + "," + safe(message);
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace(',', ';');
    }

    @Override
    public String toString() {
        String ack = acknowledged ? " [ACK]" : " [PENDING]";
        return "[" + id + "] " + department + ack
             + " | Priority: " + priority
             + " | Trend #" + trendId
             + " | " + alertDate
             + "\n  " + message;
    }

    public int      getId()           { return id; }
    public int      getTrendId()      { return trendId; }
    public String   getDepartment()   { return department; }
    public Priority getPriority()     { return priority; }
    public String   getAlertDate()    { return alertDate; }
    public boolean  isAcknowledged()  { return acknowledged; }
    public String   getMessage()      { return message; }

    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
