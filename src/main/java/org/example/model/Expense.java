package org.example.model;

/**
 * CSV format: id,department,category,amount,description,date
 */
public class Expense {

    public enum Category { MATERIALS, LABOR, MARKETING, OVERHEAD, LEGAL, OTHER }

    private int id;
    private String department;
    private Category category;
    private double amount;
    private String description;
    private String date;

    public Expense(int id, String department, Category category,
                   double amount, String description, String date) {
        this.id = id;
        this.department = department;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public String toCSV() {
        return id + "," + department + "," + category.name() + "," +
               String.format("%.2f", amount) + "," + description + "," + date;
    }

    public static Expense fromCSV(String line) {
        String[] p = line.split(",", 6);
        return new Expense(
            Integer.parseInt(p[0].trim()),
            p[1].trim(),
            Category.valueOf(p[2].trim()),
            Double.parseDouble(p[3].trim()),
            p[4].trim(),
            p[5].trim()
        );
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | $%.2f | %s | %s",
                id, department, category, amount, description, date);
    }

    public int getId()             { return id; }
    public String getDepartment()  { return department; }
    public Category getCategory()  { return category; }
    public double getAmount()      { return amount; }
    public String getDescription() { return description; }
    public String getDate()        { return date; }
}
