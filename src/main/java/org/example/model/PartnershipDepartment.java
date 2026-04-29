package org.example.model;

import org.example.pattern.visitor.DepartmentVisitor;
import org.example.pattern.visitor.VisitableDepartment;

/**
 * A regional Wholesale & Retail Partnerships unit responsible for onboarding
 * and managing wholesale partners (department stores, boutiques, distributors).
 *
 * CSV format: id,name,accountManager,budget,spentBudget,region,status,channel,maxPartners
 */
public class PartnershipDepartment implements VisitableDepartment {

    public enum Status { ACTIVE, PLANNING, ON_HOLD, CLOSED }

    private int id;
    private String name;
    private String accountManager;
    private double budget;
    private double spentBudget;
    private String region;
    private Status status;
    private String channel;
    private int maxPartners;

    public PartnershipDepartment(int id, String name, String accountManager, double budget,
                                 double spentBudget, String region, Status status,
                                 String channel, int maxPartners) {
        this.id = id;
        this.name = name;
        this.accountManager = accountManager;
        this.budget = budget;
        this.spentBudget = spentBudget;
        this.region = region;
        this.status = status;
        this.channel = channel;
        this.maxPartners = maxPartners;
    }

    public String toCSV() {
        return id + "," + name + "," + accountManager + "," + budget + "," + spentBudget
             + "," + region + "," + status.name() + "," + channel + "," + maxPartners;
    }

    public static PartnershipDepartment fromCSV(String line) {
        String[] parts = line.split(",", 9);
        return new PartnershipDepartment(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            Double.parseDouble(parts[3].trim()),
            Double.parseDouble(parts[4].trim()),
            parts[5].trim(),
            Status.valueOf(parts[6].trim()),
            parts[7].trim(),
            Integer.parseInt(parts[8].trim())
        );
    }

    public double getRemainingBudget() {
        return budget - spentBudget;
    }

    public double getBudgetUtilization() {
        return (budget > 0) ? (spentBudget / budget * 100.0) : 0.0;
    }

    @Override
    public void accept(DepartmentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Manager: " + accountManager
             + " | Budget: $" + String.format("%.2f", budget)
             + " | Spent: $" + String.format("%.2f", spentBudget)
             + " | Remaining: $" + String.format("%.2f", getRemainingBudget())
             + " | Region: " + region + " | Status: " + status
             + " | Channel: " + channel
             + " | Max Partners: " + maxPartners;
    }

    public int getId()                  { return id; }
    public String getName()             { return name; }
    public String getAccountManager()   { return accountManager; }
    public double getBudget()           { return budget; }
    public double getSpentBudget()      { return spentBudget; }
    public String getRegion()           { return region; }
    public Status getStatus()           { return status; }
    public String getChannel()          { return channel; }
    public int getMaxPartners()         { return maxPartners; }

    public void setStatus(Status status)           { this.status = status; }
    public void setSpentBudget(double spent)       { this.spentBudget = spent; }
    public void setAccountManager(String manager)  { this.accountManager = manager; }
    public void setBudget(double budget)           { this.budget = budget; }
}
