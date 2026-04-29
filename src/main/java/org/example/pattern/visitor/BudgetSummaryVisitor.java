package org.example.pattern.visitor;

import org.example.model.DesignDepartment;
import org.example.model.PartnershipDepartment;

/**
 * Walks every department type and accumulates budget metrics.
 * The reporting logic lives here, NOT inside the department classes —
 * this is the whole point of the Visitor pattern.
 */
public class BudgetSummaryVisitor implements DepartmentVisitor {

    private double totalAllocated;
    private double totalSpent;
    private int designDeptCount;
    private int partnershipDeptCount;

    @Override
    public void visit(DesignDepartment department) {
        totalAllocated += department.getBudget();
        totalSpent     += department.getSpentBudget();
        designDeptCount++;
    }

    @Override
    public void visit(PartnershipDepartment department) {
        totalAllocated += department.getBudget();
        totalSpent     += department.getSpentBudget();
        partnershipDeptCount++;
    }

    public double getTotalAllocated() { return totalAllocated; }
    public double getTotalSpent()     { return totalSpent; }
    public double getTotalRemaining() { return totalAllocated - totalSpent; }

    public String summary() {
        double utilization = (totalAllocated > 0) ? (totalSpent / totalAllocated * 100.0) : 0.0;
        return "Departments visited: design=" + designDeptCount
             + ", partnership=" + partnershipDeptCount
             + " | Allocated: $" + String.format("%.2f", totalAllocated)
             + " | Spent: $" + String.format("%.2f", totalSpent)
             + " | Remaining: $" + String.format("%.2f", getTotalRemaining())
             + " | Utilization: " + String.format("%.1f", utilization) + "%";
    }
}
