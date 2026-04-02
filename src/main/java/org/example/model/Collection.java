package org.example.model;

/**
 * Represents a fashion collection (e.g. "Summer 2025 - Coastal Line").
 * UC1: Create Collection
 *
 * CSV format: id,name,season,releasePeriod,description
 */
public class Collection {

    private int id;
    private String name;
    private String season;
    private String releasePeriod;
    private String description;

    public Collection(int id, String name, String season, String releasePeriod, String description) {
        this.id = id;
        this.name = name;
        this.season = season;
        this.releasePeriod = releasePeriod;
        this.description = description;
    }

    /** Serialize to a single CSV line. */
    public String toCSV() {
        return id + "," + name + "," + season + "," + releasePeriod + "," + description;
    }

    /** Deserialize from a single CSV line. */
    public static Collection fromCSV(String line) {
        String[] parts = line.split(",", 5);
        return new Collection(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Season: " + season +
               " | Release: " + releasePeriod + " | " + description;
    }

    // --- Getters ---
    public int getId()           { return id; }
    public String getName()      { return name; }
    public String getSeason()    { return season; }
    public String getReleasePeriod() { return releasePeriod; }
    public String getDescription() { return description; }
}
