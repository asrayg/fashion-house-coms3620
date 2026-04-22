package org.example.controller;

import org.example.model.Employee;
import org.example.util.FileManager;

import java.util.List;
import java.util.Scanner;

/**
 * UC-FA1: Manage Employee Record
 * Actor: HR Administrator
 */
public class EmployeeController {

    static final String FILE = "data/hr/employees.csv";

    private final Scanner scanner;

    public EmployeeController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Employee Records ---");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employee by ID");
            System.out.println("3. List All Employees");
            System.out.println("4. Update Employee");
            System.out.println("5. Deactivate Employee");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> addEmployee();
                case "2" -> viewEmployee();
                case "3" -> listEmployees();
                case "4" -> updateEmployee();
                case "5" -> deactivateEmployee();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC-FA1 — Add Employee
    // -------------------------------------------------------------------------

    private void addEmployee() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Error: Name cannot be blank."); return; }

        System.out.print("Role/Title: ");
        String role = scanner.nextLine().trim();
        if (role.isEmpty()) { System.out.println("Error: Role cannot be blank."); return; }

        System.out.print("Department: ");
        String department = scanner.nextLine().trim();
        if (department.isEmpty()) { System.out.println("Error: Department cannot be blank."); return; }

        System.out.println("Employment Type: 1) FULL_TIME  2) PART_TIME  3) CONTRACT");
        System.out.print("Select: ");
        Employee.EmploymentType empType;
        switch (scanner.nextLine().trim()) {
            case "1" -> empType = Employee.EmploymentType.FULL_TIME;
            case "2" -> empType = Employee.EmploymentType.PART_TIME;
            case "3" -> empType = Employee.EmploymentType.CONTRACT;
            default  -> { System.out.println("Error: Invalid employment type."); return; }
        }

        System.out.print("Hire Date (YYYY-MM-DD): ");
        String hireDate = scanner.nextLine().trim();
        if (hireDate.isEmpty()) { System.out.println("Error: Hire date cannot be blank."); return; }

        System.out.print("Base Salary ($): ");
        double baseSalary;
        try {
            baseSalary = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Salary must be a number.");
            return;
        }
        if (baseSalary < 0) { System.out.println("Error: Salary cannot be negative."); return; }

        int id = FileManager.nextId(FILE);
        Employee emp = new Employee(id, name, role, department, empType, hireDate, baseSalary, Employee.Status.ACTIVE);
        FileManager.appendLine(FILE, emp.toCSV());
        System.out.println("Employee added successfully:");
        System.out.println(emp);
    }

    // -------------------------------------------------------------------------
    // View Employee by ID
    // -------------------------------------------------------------------------

    private void viewEmployee() {
        System.out.print("Enter Employee ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a whole number.");
            return;
        }
        Employee emp = findById(id);
        if (emp == null) {
            System.out.println("Error: No employee found with ID " + id + ".");
        } else {
            System.out.println(emp);
        }
    }

    // -------------------------------------------------------------------------
    // List All Employees
    // -------------------------------------------------------------------------

    private void listEmployees() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) { System.out.println("No employee records on file."); return; }
        System.out.println("\n--- All Employees ---");
        for (String line : lines) {
            System.out.println(Employee.fromCSV(line));
        }
    }

    // -------------------------------------------------------------------------
    // Update Employee (role, department, or salary)
    // -------------------------------------------------------------------------

    private void updateEmployee() {
        if (!FileManager.hasRecords(FILE)) { System.out.println("No employee records on file."); return; }

        System.out.print("Enter Employee ID to update: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a whole number.");
            return;
        }
        Employee emp = findById(id);
        if (emp == null) { System.out.println("Error: No employee found with ID " + id + "."); return; }

        System.out.println("Current record: " + emp);
        System.out.println("What would you like to update?");
        System.out.println("1. Role   2. Department   3. Base Salary");
        System.out.print("Select: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> {
                System.out.print("New Role: ");
                String newRole = scanner.nextLine().trim();
                if (newRole.isEmpty()) { System.out.println("Error: Role cannot be blank."); return; }
                emp.setRole(newRole);
            }
            case "2" -> {
                System.out.print("New Department: ");
                String newDept = scanner.nextLine().trim();
                if (newDept.isEmpty()) { System.out.println("Error: Department cannot be blank."); return; }
                emp.setDepartment(newDept);
            }
            case "3" -> {
                System.out.print("New Base Salary ($): ");
                try {
                    double newSalary = Double.parseDouble(scanner.nextLine().trim());
                    if (newSalary < 0) { System.out.println("Error: Salary cannot be negative."); return; }
                    emp.setBaseSalary(newSalary);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Salary must be a number.");
                    return;
                }
            }
            default -> { System.out.println("Invalid option."); return; }
        }
        update(emp);
        System.out.println("Employee updated:");
        System.out.println(emp);
    }

    // -------------------------------------------------------------------------
    // Deactivate Employee
    // -------------------------------------------------------------------------

    private void deactivateEmployee() {
        if (!FileManager.hasRecords(FILE)) { System.out.println("No employee records on file."); return; }

        System.out.print("Enter Employee ID to deactivate: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a whole number.");
            return;
        }
        Employee emp = findById(id);
        if (emp == null) { System.out.println("Error: No employee found with ID " + id + "."); return; }
        if (emp.getStatus() == Employee.Status.INACTIVE) {
            System.out.println("Employee is already inactive.");
            return;
        }
        emp.setStatus(Employee.Status.INACTIVE);
        update(emp);
        System.out.println("Employee deactivated: " + emp.getName());
    }

    // -------------------------------------------------------------------------
    // Static helpers (used by PayrollController)
    // -------------------------------------------------------------------------

    public static Employee findById(int id) {
        for (String line : FileManager.readLines(FILE)) {
            Employee e = Employee.fromCSV(line);
            if (e.getId() == id) return e;
        }
        return null;
    }

    public static void update(Employee updated) {
        List<String> lines = FileManager.readLines(FILE);
        for (int i = 0; i < lines.size(); i++) {
            Employee e = Employee.fromCSV(lines.get(i));
            if (e.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(FILE, lines);
    }
}
