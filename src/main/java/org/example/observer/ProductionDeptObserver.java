package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.util.FileManager;

import java.time.LocalDate;

/**
 * Observer for the Production department.
 * Demonstrates the open–closed nature of the Observer pattern:
 * adding a third subscriber required zero changes to the subject.
 */
public class ProductionDeptObserver implements TrendObserver {

    private static final String DEPARTMENT  = "Production";
    private static final String ALERTS_FILE = "data/research/trend_alerts.csv";


    public ProductionDeptObserver() {
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        // Hardcoded relevance logic for Production department
        if (trend.getCategory() != org.example.model.MarketTrend.Category.FABRIC) {
            System.out.println("  [Observer] Production Dept filtered out '" + trend.getName() + "' (not relevant).");
            return;
        }
        TrendAlert.Priority priority = TrendAlert.Priority.LOW;
        String message = "Production Alert: " + trend.getName() + " is relevant to production operations.";
        TrendAlert alert = new TrendAlert(
            FileManager.nextId(ALERTS_FILE),
            trend.getId(),
            DEPARTMENT,
            priority,
            LocalDate.now().toString(),
            false,
            message
        );
        FileManager.appendLine(ALERTS_FILE, alert.toCSV());
        System.out.println("  [Observer] Production Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
