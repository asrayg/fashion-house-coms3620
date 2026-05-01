package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.util.FileManager;

import java.time.LocalDate;

/**
 * Observer for the Marketing & Communications department.
 * Handles trend relevance and alert creation for Marketing department.
 */
public class MarketingDeptObserver implements TrendObserver {

    private static final String DEPARTMENT  = "Marketing";
    private static final String ALERTS_FILE = "data/research/trend_alerts.csv";


    public MarketingDeptObserver() {
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        // Hardcoded relevance logic for Marketing department
        if (trend.getCategory() != org.example.model.MarketTrend.Category.CONSUMER_BEHAVIOR) {
            System.out.println("  [Observer] Marketing Dept filtered out '" + trend.getName() + "' (not relevant).");
            return;
        }
        TrendAlert.Priority priority = TrendAlert.Priority.MEDIUM;
        String message = "Marketing Alert: " + trend.getName() + " is relevant to marketing campaigns.";
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
        System.out.println("  [Observer] Marketing Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
