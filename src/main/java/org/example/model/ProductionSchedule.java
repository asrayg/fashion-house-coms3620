package org.example.model;

public class ProductionSchedule {
    private int id;
    private int allocationId;
    private String productionLineId;
    private String startDate;
    private String endDate;
    private int estimatedDuration; // in hours
    private String resourceRequirements;
    private String bottleneckNotes;
    private String scheduleStatus; // feasible, at-risk, infeasible
    private String createdDate;

    public ProductionSchedule(int id, int allocationId, String productionLineId, String startDate,
                             String endDate, int estimatedDuration, String resourceRequirements,
                             String bottleneckNotes, String scheduleStatus, String createdDate) {
        this.id = id;
        this.allocationId = allocationId;
        this.productionLineId = productionLineId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.estimatedDuration = estimatedDuration;
        this.resourceRequirements = resourceRequirements;
        this.bottleneckNotes = bottleneckNotes;
        this.scheduleStatus = scheduleStatus;
        this.createdDate = createdDate;
    }

    public String toCSV() {
        return id + "," + allocationId + "," + productionLineId + "," + startDate + "," +
               endDate + "," + estimatedDuration + "," + resourceRequirements + "," +
               bottleneckNotes + "," + scheduleStatus + "," + createdDate;
    }

    public static ProductionSchedule fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new ProductionSchedule(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            parts[2],
            parts[3],
            parts[4],
            Integer.parseInt(parts[5]),
            parts[6],
            parts[7],
            parts[8],
            parts[9]
        );
    }

    public int getId() { return id; }
    public int getAllocationId() { return allocationId; }
    public String getProductionLineId() { return productionLineId; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getEstimatedDuration() { return estimatedDuration; }
    public String getResourceRequirements() { return resourceRequirements; }
    public String getBottleneckNotes() { return bottleneckNotes; }
    public String getScheduleStatus() { return scheduleStatus; }
    public String getCreatedDate() { return createdDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setScheduleStatus(String status) { this.scheduleStatus = status; }

    @Override
    public String toString() {
        return "ProductionSchedule{id=" + id + ", line='" + productionLineId +
               "', " + startDate + " to " + endDate + ", status='" + scheduleStatus + "'}";
    }
}