package org.example.model;

/**
 * A garment design belonging to a collection.
 * UC2: Add Garment Design
 *
 * CSV format: id,collectionId,name,type,style,targetAudience,notes
 */
public class GarmentDesign {

    private int id;
    private int collectionId;
    private String name;
    private String type;
    private String style;
    private String targetAudience;
    private String notes;

    public GarmentDesign(int id, int collectionId, String name, String type,
                         String style, String targetAudience, String notes) {
        this.id = id;
        this.collectionId = collectionId;
        this.name = name;
        this.type = type;
        this.style = style;
        this.targetAudience = targetAudience;
        this.notes = notes;
    }

    public String toCSV() {
        return id + "," + collectionId + "," + name + "," + type + "," +
               style + "," + targetAudience + "," + notes;
    }

    public static GarmentDesign fromCSV(String line) {
        String[] parts = line.split(",", 7);
        return new GarmentDesign(
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim()),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            parts[5].trim(),
            parts[6].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Type: " + type + " | Style: " + style +
               " | Audience: " + targetAudience + " | Notes: " + notes +
               " | CollectionID: " + collectionId;
    }

    // --- Getters ---
    public int getId()             { return id; }
    public int getCollectionId()   { return collectionId; }
    public String getName()        { return name; }
    public String getType()        { return type; }
    public String getStyle()       { return style; }
    public String getTargetAudience() { return targetAudience; }
    public String getNotes()       { return notes; }
}
