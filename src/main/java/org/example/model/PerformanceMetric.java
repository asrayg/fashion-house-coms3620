package org.example.model;

public class PerformanceMetric {
    private int id;
    private String reportPeriodStart;
    private String reportPeriodEnd;
    private int totalDesignsProduced;
    private int totalQuantityProduced;
    private double onTimeCompletionPercent;
    private double averageWastePercent;
    private double qualityPassRate;
    private double lineUtilizationPercent;
    private double costPerGarment;
    private String topPerformingDesign;
    private String underperformingDesign;
    private String generatedDate;

    public PerformanceMetric(int id, String reportPeriodStart, String reportPeriodEnd,
                            int totalDesignsProduced, int totalQuantityProduced,
                            double onTimeCompletionPercent, double averageWastePercent,
                            double qualityPassRate, double lineUtilizationPercent,
                            double costPerGarment, String topPerformingDesign,
                            String underperformingDesign, String generatedDate) {
        this.id = id;
        this.reportPeriodStart = reportPeriodStart;
        this.reportPeriodEnd = reportPeriodEnd;
        this.totalDesignsProduced = totalDesignsProduced;
        this.totalQuantityProduced = totalQuantityProduced;
        this.onTimeCompletionPercent = onTimeCompletionPercent;
        this.averageWastePercent = averageWastePercent;
        this.qualityPassRate = qualityPassRate;
        this.lineUtilizationPercent = lineUtilizationPercent;
        this.costPerGarment = costPerGarment;
        this.topPerformingDesign = topPerformingDesign;
        this.underperformingDesign = underperformingDesign;
        this.generatedDate = generatedDate;
    }

    public String toCSV() {
        return id + "," + reportPeriodStart + "," + reportPeriodEnd + "," + totalDesignsProduced + "," +
               totalQuantityProduced + "," + onTimeCompletionPercent + "," + averageWastePercent + "," +
               qualityPassRate + "," + lineUtilizationPercent + "," + costPerGarment + "," +
               topPerformingDesign + "," + underperformingDesign + "," + generatedDate;
    }

    public static PerformanceMetric fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new PerformanceMetric(
            Integer.parseInt(parts[0]),
            parts[1],
            parts[2],
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4]),
            Double.parseDouble(parts[5]),
            Double.parseDouble(parts[6]),
            Double.parseDouble(parts[7]),
            Double.parseDouble(parts[8]),
            Double.parseDouble(parts[9]),
            parts[10],
            parts[11],
            parts[12]
        );
    }

    public int getId() { return id; }
    public String getReportPeriodStart() { return reportPeriodStart; }
    public String getReportPeriodEnd() { return reportPeriodEnd; }
    public int getTotalDesignsProduced() { return totalDesignsProduced; }
    public int getTotalQuantityProduced() { return totalQuantityProduced; }
    public double getOnTimeCompletionPercent() { return onTimeCompletionPercent; }
    public double getAverageWastePercent() { return averageWastePercent; }
    public double getQualityPassRate() { return qualityPassRate; }
    public double getLineUtilizationPercent() { return lineUtilizationPercent; }
    public double getCostPerGarment() { return costPerGarment; }
    public String getTopPerformingDesign() { return topPerformingDesign; }
    public String getUnderperformingDesign() { return underperformingDesign; }
    public String getGeneratedDate() { return generatedDate; }

    @Override
    public String toString() {
        return "PerformanceMetric{id=" + id + ", period=" + reportPeriodStart + " to " + reportPeriodEnd +
               ", designs=" + totalDesignsProduced + ", qty=" + totalQuantityProduced +
               ", onTime=" + String.format("%.2f", onTimeCompletionPercent) + "%}";
    }
}