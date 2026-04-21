package org.example.model;

/**
 * CSV format: id,employeeId,employeeName,payPeriodStart,payPeriodEnd,grossPay,taxRate,taxDeduction,netPay,generatedDate
 */
public class PayrollRecord {

    private int id;
    private int employeeId;
    private String employeeName;
    private String payPeriodStart;
    private String payPeriodEnd;
    private double grossPay;
    private double taxRate;
    private double taxDeduction;
    private double netPay;
    private String generatedDate;

    public PayrollRecord(int id, int employeeId, String employeeName,
                         String payPeriodStart, String payPeriodEnd,
                         double grossPay, double taxRate, double taxDeduction,
                         double netPay, String generatedDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.grossPay = grossPay;
        this.taxRate = taxRate;
        this.taxDeduction = taxDeduction;
        this.netPay = netPay;
        this.generatedDate = generatedDate;
    }

    public String toCSV() {
        return id + "," + employeeId + "," + employeeName + "," +
               payPeriodStart + "," + payPeriodEnd + "," +
               String.format("%.2f", grossPay) + "," +
               String.format("%.2f", taxRate) + "," +
               String.format("%.2f", taxDeduction) + "," +
               String.format("%.2f", netPay) + "," +
               generatedDate;
    }

    public static PayrollRecord fromCSV(String line) {
        String[] p = line.split(",", 10);
        return new PayrollRecord(
            Integer.parseInt(p[0].trim()),
            Integer.parseInt(p[1].trim()),
            p[2].trim(),
            p[3].trim(),
            p[4].trim(),
            Double.parseDouble(p[5].trim()),
            Double.parseDouble(p[6].trim()),
            Double.parseDouble(p[7].trim()),
            Double.parseDouble(p[8].trim()),
            p[9].trim()
        );
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (ID:%d) | Period: %s to %s | Gross: $%.2f | Tax(%.0f%%): $%.2f | Net: $%.2f",
                id, employeeName, employeeId, payPeriodStart, payPeriodEnd,
                grossPay, taxRate, taxDeduction, netPay);
    }

    public int getId()              { return id; }
    public int getEmployeeId()      { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getPayPeriodStart() { return payPeriodStart; }
    public String getPayPeriodEnd()   { return payPeriodEnd; }
    public double getGrossPay()     { return grossPay; }
    public double getTaxRate()      { return taxRate; }
    public double getTaxDeduction() { return taxDeduction; }
    public double getNetPay()       { return netPay; }
    public String getGeneratedDate() { return generatedDate; }
}
