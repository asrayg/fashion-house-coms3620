package org.example.model;

/**
 * CSV format: id,name,role,department,employmentType,hireDate,baseSalary,status
 */
public class Employee {

    public enum EmploymentType { FULL_TIME, PART_TIME, CONTRACT }
    public enum Status { ACTIVE, INACTIVE }

    private int id;
    private String name;
    private String role;
    private String department;
    private EmploymentType employmentType;
    private String hireDate;
    private double baseSalary;
    private Status status;

    public Employee(int id, String name, String role, String department,
                    EmploymentType employmentType, String hireDate,
                    double baseSalary, Status status) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.employmentType = employmentType;
        this.hireDate = hireDate;
        this.baseSalary = baseSalary;
        this.status = status;
    }

    public String toCSV() {
        return id + "," + name + "," + role + "," + department + "," +
               employmentType.name() + "," + hireDate + "," + baseSalary + "," + status.name();
    }

    public static Employee fromCSV(String line) {
        String[] p = line.split(",", 8);
        return new Employee(
            Integer.parseInt(p[0].trim()),
            p[1].trim(),
            p[2].trim(),
            p[3].trim(),
            EmploymentType.valueOf(p[4].trim()),
            p[5].trim(),
            Double.parseDouble(p[6].trim()),
            Status.valueOf(p[7].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " | Role: " + role + " | Dept: " + department +
               " | Type: " + employmentType + " | Hired: " + hireDate +
               " | Salary: $" + String.format("%.2f", baseSalary) + " | Status: " + status;
    }

    public int getId()                    { return id; }
    public String getName()               { return name; }
    public String getRole()               { return role; }
    public String getDepartment()         { return department; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public String getHireDate()           { return hireDate; }
    public double getBaseSalary()         { return baseSalary; }
    public Status getStatus()             { return status; }

    public void setRole(String role)           { this.role = role; }
    public void setDepartment(String department) { this.department = department; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }
    public void setStatus(Status status)       { this.status = status; }
}
