package org.example.model;

/**
 * Represents a physical production line in the factory.
 * Each line has a daily capacity and operational status.
 *
 * CSV format: id,name,capacityPerDay,status,location
 */
public class ProductionLine {

    public enum Status { ACTIVE, MAINTENANCE, INACTIVE }

    private int id;
    private String name;
    private int capacityPerDay;
    private Status status;
    private String location;

    public ProductionLine(int id, String name, int capacityPerDay,
                          Status status, String location) {
        this.id = id;
        this.name = name;
        this.capacityPerDay = capacityPerDay;
        this.status = status;
        this.location = location;
    }

    public String toCSV() {
        return id + "," + name + "," + capacityPerDay + "," + status.name() + "," + location;
    }

    public static ProductionLine fromCSV(String line) {
        String[] parts = line.split(",", 5);
        return new ProductionLine(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            Integer.parseInt(parts[2].trim()),
            Status.valueOf(parts[3].trim()),
            parts[4].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Capacity: " + capacityPerDay
             + "/day | Status: " + status + " | Location: " + location;
    }

    // --- Getters ---
    public int getId()              { return id; }
    public String getName()         { return name; }
    public int getCapacityPerDay()  { return capacityPerDay; }
    public Status getStatus()       { return status; }
    public String getLocation()     { return location; }

    // --- Setters ---
    public void setStatus(Status status) { this.status = status; }
}
