package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.util.FileManager;

import java.time.LocalDate;

/**
 * Observer for the Design department.
 * Handles trend relevance and alert creation for Design department.
 */
public class DesignDeptObserver implements TrendObserver {

    private static final String DEPARTMENT  = "Design";
    private static final String ALERTS_FILE = "data/research/trend_alerts.csv";


    public DesignDeptObserver() {
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        // Hardcoded relevance logic for Design department
        if (trend.getCategory() != org.example.model.MarketTrend.Category.FASHION) {
            System.out.println("  [Observer] Design Dept filtered out '" + trend.getName() + "' (not relevant).");
            return;
        }
        TrendAlert.Priority priority = TrendAlert.Priority.HIGH;
        String message = "Design Alert: " + trend.getName() + " is relevant to design initiatives.";
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
        System.out.println("  [Observer] Design Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
