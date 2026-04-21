package org.example.model;

/**
 * CSV format: id,periodStart,periodEnd,totalRevenue,materialCosts,payrollCosts,
 *             otherExpenses,marketingSpend,grossProfit,netProfit,generatedDate
 */
public class FinancialReport {

    private int id;
    private String periodStart;
    private String periodEnd;
    private double totalRevenue;
    private double materialCosts;
    private double payrollCosts;
    private double otherExpenses;
    private double marketingSpend;
    private double grossProfit;
    private double netProfit;
    private String generatedDate;

    public FinancialReport(int id, String periodStart, String periodEnd,
                           double totalRevenue, double materialCosts, double payrollCosts,
                           double otherExpenses, double marketingSpend,
                           double grossProfit, double netProfit, String generatedDate) {
        this.id = id;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalRevenue = totalRevenue;
        this.materialCosts = materialCosts;
        this.payrollCosts = payrollCosts;
        this.otherExpenses = otherExpenses;
        this.marketingSpend = marketingSpend;
        this.grossProfit = grossProfit;
        this.netProfit = netProfit;
        this.generatedDate = generatedDate;
    }

    public String toCSV() {
        return id + "," + periodStart + "," + periodEnd + "," +
               String.format("%.2f", totalRevenue) + "," +
               String.format("%.2f", materialCosts) + "," +
               String.format("%.2f", payrollCosts) + "," +
               String.format("%.2f", otherExpenses) + "," +
               String.format("%.2f", marketingSpend) + "," +
               String.format("%.2f", grossProfit) + "," +
               String.format("%.2f", netProfit) + "," +
               generatedDate;
    }

    public static FinancialReport fromCSV(String line) {
        String[] p = line.split(",", 11);
        return new FinancialReport(
            Integer.parseInt(p[0].trim()),
            p[1].trim(), p[2].trim(),
            Double.parseDouble(p[3].trim()),
            Double.parseDouble(p[4].trim()),
            Double.parseDouble(p[5].trim()),
            Double.parseDouble(p[6].trim()),
            Double.parseDouble(p[7].trim()),
            Double.parseDouble(p[8].trim()),
            Double.parseDouble(p[9].trim()),
            p[10].trim()
        );
    }

    @Override
    public String toString() {
        return String.format("[%d] Period: %s to %s | Revenue: $%.2f | Gross Profit: $%.2f | Net Profit: $%.2f",
                id, periodStart, periodEnd, totalRevenue, grossProfit, netProfit);
    }

    public int getId()              { return id; }
    public String getPeriodStart()  { return periodStart; }
    public String getPeriodEnd()    { return periodEnd; }
    public double getTotalRevenue() { return totalRevenue; }
    public double getMaterialCosts(){ return materialCosts; }
    public double getPayrollCosts() { return payrollCosts; }
    public double getOtherExpenses(){ return otherExpenses; }
    public double getMarketingSpend(){ return marketingSpend; }
    public double getGrossProfit()  { return grossProfit; }
    public double getNetProfit()    { return netProfit; }
    public String getGeneratedDate(){ return generatedDate; }
}
