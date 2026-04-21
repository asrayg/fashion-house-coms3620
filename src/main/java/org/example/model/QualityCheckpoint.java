package org.example.model;

public class QualityCheckpoint {
    private int id;
    private int allocationId;
    private String batchId;
    private int unitsTested;
    private int unitsPassed;
    private int unitsFailed;
    private String checkpointDate;
    private String defectNotes;
    private String severity; // low, medium, high, critical
    private String status; // pending, approved, rework, rejected

    public QualityCheckpoint(int id, int allocationId, String batchId, int unitsTested,
                            int unitsPassed, int unitsFailed, String checkpointDate,
                            String defectNotes, String severity, String status) {
        this.id = id;
        this.allocationId = allocationId;
        this.batchId = batchId;
        this.unitsTested = unitsTested;
        this.unitsPassed = unitsPassed;
        this.unitsFailed = unitsFailed;
        this.checkpointDate = checkpointDate;
        this.defectNotes = defectNotes;
        this.severity = severity;
        this.status = status;
    }

    public String toCSV() {
        return id + "," + allocationId + "," + batchId + "," + unitsTested + "," +
               unitsPassed + "," + unitsFailed + "," + checkpointDate + "," +
               defectNotes + "," + severity + "," + status;
    }

    public static QualityCheckpoint fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new QualityCheckpoint(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            parts[2],
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4]),
            Integer.parseInt(parts[5]),
            parts[6],
            parts[7],
            parts[8],
            parts[9]
        );
    }

    public int getId() { return id; }
    public int getAllocationId() { return allocationId; }
    public String getBatchId() { return batchId; }
    public int getUnitsTested() { return unitsTested; }
    public int getUnitsPassed() { return unitsPassed; }
    public int getUnitsFailed() { return unitsFailed; }
    public String getCheckpointDate() { return checkpointDate; }
    public String getDefectNotes() { return defectNotes; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public double getPassRate() {
        if (unitsTested == 0) return 0;
        return (unitsPassed * 100.0) / unitsTested;
    }

    @Override
    public String toString() {
        return "QualityCheckpoint{id=" + id + ", batch='" + batchId +
               "', passRate=" + String.format("%.2f", getPassRate()) + "%, status='" + status + "'}";
    }
}