package org.example.controller;

import org.example.model.Employee;
import org.example.model.PayrollRecord;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * UC-FA2: Process Payroll
 * Actor: Finance Manager
 *
 * Salary interpretation:
 *   FULL_TIME / PART_TIME — baseSalary is annual; gross = baseSalary * (daysInPeriod / 365.0)
 *   CONTRACT              — baseSalary is hourly rate; Finance Manager enters hours worked
 */
public class PayrollController {

    static final String FILE = "data/payroll.csv";
    private static final double DEFAULT_TAX_RATE = 22.0;

    private final Scanner scanner;

    public PayrollController(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Payroll ---");
            System.out.println("1. Process Payroll Run");
            System.out.println("2. View Payroll History");
            System.out.println("0. Back");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> processPayroll();
                case "2" -> viewHistory();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // UC-FA2 — Process Payroll Run
    // -------------------------------------------------------------------------

    private void processPayroll() {
        // Precondition: at least one active employee
        List<Employee> activeEmployees = new ArrayList<>();
        for (String line : FileManager.readLines(EmployeeController.FILE)) {
            Employee e = Employee.fromCSV(line);
            if (e.getStatus() == Employee.Status.ACTIVE) activeEmployees.add(e);
        }
        if (activeEmployees.isEmpty()) {
            System.out.println("Error: No active employees found.");
            return;
        }

        // Prompt for pay period
        System.out.print("Pay Period Start (YYYY-MM-DD): ");
        String startStr = scanner.nextLine().trim();
        System.out.print("Pay Period End   (YYYY-MM-DD): ");
        String endStr = scanner.nextLine().trim();

        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startStr);
            endDate   = LocalDate.parse(endStr);
        } catch (Exception e) {
            System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        if (!endDate.isAfter(startDate)) {
            System.out.println("Error: End date must be after start date.");
            return;
        }
        long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Prompt for tax rate
        System.out.printf("Tax rate (%% of gross) [default %.0f%%]: ", DEFAULT_TAX_RATE);
        String taxInput = scanner.nextLine().trim();
        double taxRate = DEFAULT_TAX_RATE;
        if (!taxInput.isEmpty()) {
            try {
                taxRate = Double.parseDouble(taxInput);
                if (taxRate < 0 || taxRate > 100) {
                    System.out.println("Error: Tax rate must be between 0 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Tax rate must be a number.");
                return;
            }
        }

        // Preview summary
        System.out.printf("%nPay period: %s to %s (%d days) | Tax rate: %.0f%%%n",
                startStr, endStr, daysInPeriod, taxRate);
        System.out.printf("Active employees: %d%n", activeEmployees.size());
        System.out.print("Confirm payroll run? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Payroll run cancelled.");
            return;
        }

        // Collect hours for CONTRACT employees up front
        List<Double> hoursWorked = new ArrayList<>();
        for (Employee emp : activeEmployees) {
            if (emp.getEmploymentType() == Employee.EmploymentType.CONTRACT) {
                System.out.printf("Hours worked for %s (CONTRACT, $%.2f/hr): ", emp.getName(), emp.getBaseSalary());
                try {
                    double hours = Double.parseDouble(scanner.nextLine().trim());
                    if (hours < 0) { System.out.println("Error: Hours cannot be negative."); return; }
                    hoursWorked.add(hours);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Hours must be a number.");
                    return;
                }
            } else {
                hoursWorked.add(-1.0); // sentinel — not used for salaried employees
            }
        }

        // Compute and write payroll records
        String today = LocalDate.now().toString();
        double totalGross = 0, totalDeductions = 0, totalNet = 0;

        System.out.println("\n--- Payroll Run Results ---");
        for (int i = 0; i < activeEmployees.size(); i++) {
            Employee emp = activeEmployees.get(i);
            double gross;
            if (emp.getEmploymentType() == Employee.EmploymentType.CONTRACT) {
                gross = emp.getBaseSalary() * hoursWorked.get(i);
            } else {
                gross = emp.getBaseSalary() * (daysInPeriod / 365.0);
            }

            double deduction = gross * (taxRate / 100.0);
            double net = gross - deduction;

            int id = FileManager.nextId(FILE) + i;
            PayrollRecord record = new PayrollRecord(
                    id, emp.getId(), emp.getName(),
                    startStr, endStr,
                    gross, taxRate, deduction, net, today
            );
            FileManager.appendLine(FILE, record.toCSV());
            System.out.println(record);

            totalGross       += gross;
            totalDeductions  += deduction;
            totalNet         += net;
        }

        System.out.println("\n--- Summary ---");
        System.out.printf("Employees processed : %d%n", activeEmployees.size());
        System.out.printf("Total Gross Pay     : $%.2f%n", totalGross);
        System.out.printf("Total Deductions    : $%.2f%n", totalDeductions);
        System.out.printf("Total Net Pay       : $%.2f%n", totalNet);
    }

    // -------------------------------------------------------------------------
    // View Payroll History
    // -------------------------------------------------------------------------

    private void viewHistory() {
        List<String> lines = FileManager.readLines(FILE);
        if (lines.isEmpty()) { System.out.println("No payroll records on file."); return; }

        System.out.println("\n--- Payroll History ---");
        String currentPeriod = "";
        for (String line : lines) {
            PayrollRecord r = PayrollRecord.fromCSV(line);
            String period = r.getPayPeriodStart() + " to " + r.getPayPeriodEnd();
            if (!period.equals(currentPeriod)) {
                System.out.println("\nPeriod: " + period);
                currentPeriod = period;
            }
            System.out.println("  " + r);
        }
    }
}
