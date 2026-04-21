package org.example.model;

public class ProductionAllocation {
    private int id;
    private int garmentDesignId;
    private int productionLineId;
    private int quantity;
    private String deadline;
    private String status; // pending, in-progress, completed, halted
    private String createdDate;
    private String estimatedCompletionDate;

    public ProductionAllocation(int id, int garmentDesignId, int productionLineId, int quantity,
                               String deadline, String status, String createdDate, String estimatedCompletionDate) {
        this.id = id;
        this.garmentDesignId = garmentDesignId;
        this.productionLineId = productionLineId;
        this.quantity = quantity;
        this.deadline = deadline;
        this.status = status;
        this.createdDate = createdDate;
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public String toCSV() {
        return id + "," + garmentDesignId + "," + productionLineId + "," + quantity + "," +
               deadline + "," + status + "," + createdDate + "," + estimatedCompletionDate;
    }

    public static ProductionAllocation fromCSV(String line) {
        String[] parts = line.split(",");
        return new ProductionAllocation(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            parts[4],
            parts[5],
            parts[6],
            parts[7]
        );
    }

    public int getId() { return id; }
    public int getGarmentDesignId() { return garmentDesignId; }
    public int getProductionLineId() { return productionLineId; }
    public int getQuantity() { return quantity; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public String getCreatedDate() { return createdDate; }
    public String getEstimatedCompletionDate() { return estimatedCompletionDate; }

    public void setStatus(String status) { this.status = status; }
    public void setEstimatedCompletionDate(String date) { this.estimatedCompletionDate = date; }

    @Override
    public String toString() {
        return "ProductionAllocation{id=" + id + ", garmentId=" + garmentDesignId +
               ", lineId=" + productionLineId + ", qty=" + quantity +
               ", deadline='" + deadline + "', status='" + status + "'}";
    }
}