package org.example.model;

import org.example.pattern.visitor.DepartmentVisitor;
import org.example.pattern.visitor.VisitableDepartment;

/**
 * Represents a design department within the fashion house.
 * Each department focuses on a specialization and season, has a budget, and a head designer.
 *
 * CSV format: id,name,headDesigner,budget,spentBudget,season,status,specialization,maxCapacity
 */
public class DesignDepartment implements VisitableDepartment {

    public enum Status { ACTIVE, PLANNING, ON_HOLD, CLOSED }

    private int id;
    private String name;
    private String headDesigner;
    private double budget;
    private double spentBudget;
    private String season;
    private Status status;
    private String specialization;
    private int maxCapacity;

    public DesignDepartment(int id, String name, String headDesigner, double budget,
                            double spentBudget, String season, Status status,
                            String specialization, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.headDesigner = headDesigner;
        this.budget = budget;
        this.spentBudget = spentBudget;
        this.season = season;
        this.status = status;
        this.specialization = specialization;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public void accept(DepartmentVisitor visitor) {
        visitor.visit(this);
    }

    public String toCSV() {
        return id + "," + name + "," + headDesigner + "," + budget + "," + spentBudget
             + "," + season + "," + status.name() + "," + specialization + "," + maxCapacity;
    }

    public static DesignDepartment fromCSV(String line) {
        String[] parts = line.split(",", 9);
        return new DesignDepartment(
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

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Head: " + headDesigner
             + " | Budget: $" + String.format("%.2f", budget)
             + " | Spent: $" + String.format("%.2f", spentBudget)
             + " | Remaining: $" + String.format("%.2f", budget - spentBudget)
             + " | Season: " + season + " | Status: " + status
             + " | Specialization: " + specialization
             + " | Capacity: " + maxCapacity;
    }

    // --- Getters ---
    public int getId()               { return id; }
    public String getName()          { return name; }
    public String getHeadDesigner()  { return headDesigner; }
    public double getBudget()        { return budget; }
    public double getSpentBudget()   { return spentBudget; }
    public String getSeason()        { return season; }
    public Status getStatus()        { return status; }
    public String getSpecialization() { return specialization; }
    public int getMaxCapacity()      { return maxCapacity; }

    // --- Setters ---
    public void setStatus(Status status)         { this.status = status; }
    public void setSpentBudget(double spent)     { this.spentBudget = spent; }
    public void setHeadDesigner(String head)     { this.headDesigner = head; }
    public void setBudget(double budget)         { this.budget = budget; }
}
