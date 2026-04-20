package org.example.model;

/**
 * Represents an ad campaign linked to a collection.
 * Supports multiple platforms with per-platform budgets and lifecycle status.
 *
 * CSV format:
 * id,collectionId,name,platforms(|sep),platformBudgets(|sep),totalBudget,startDate,status
 */
public class AdCampaign {

    private int id;
    private int collectionId;
    private String name;
    private String platforms;        
    private String platformBudgets;  
    private double totalBudget;
    private String startDate;
    private String status;

    public AdCampaign(int id, int collectionId, String name,
                      String platforms, String platformBudgets,
                      double totalBudget, String startDate, String status) {
        this.id = id;
        this.collectionId = collectionId;
        this.name = name;
        this.platforms = platforms;
        this.platformBudgets = platformBudgets;
        this.totalBudget = totalBudget;
        this.startDate = startDate;
        this.status = status;
    }

    public String toCSV() {
        return id + "," + collectionId + "," + name + ","
                + platforms + "," + platformBudgets + ","
                + totalBudget + "," + startDate + "," + status;
    }

    public static AdCampaign fromCSV(String line) {
        String[] parts = line.split(",", 8);
        return new AdCampaign(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            Double.parseDouble(parts[5].trim()),
            parts[6].trim(),
            parts[7].trim()
        );
    }

    @Override
    public String toString() {
        String[] platformList = platforms.split("\\|");
        String[] budgetList = platformBudgets.split("\\|");
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(id).append("] ")
          .append(name)
          .append(" | Collection: ").append(collectionId)
          .append(" | Status: ").append(status)
          .append(" | Start: ").append(startDate)
          .append(" | Total Budget: $").append(totalBudget)
          .append("\n  Platforms:");
        for (int i = 0; i < platformList.length; i++) {
            sb.append("\n    - ").append(platformList[i].trim())
              .append(": $").append(budgetList[i].trim());
        }
        return sb.toString();
    }

    public int getId()                  { return id; }
    public int getCollectionId()        { return collectionId; }
    public String getName()             { return name; }
    public String getPlatforms()        { return platforms; }
    public String getPlatformBudgets()  { return platformBudgets; }
    public double getTotalBudget()      { return totalBudget; }
    public String getStartDate()        { return startDate; }
    public String getStatus()           { return status; }
}
