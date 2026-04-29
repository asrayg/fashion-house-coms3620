package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.strategy.ProductionRelevanceStrategy;
import org.example.strategy.TrendRelevanceStrategy;
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

    private final TrendRelevanceStrategy strategy;

    public ProductionDeptObserver() {
        this(new ProductionRelevanceStrategy());
    }

    public ProductionDeptObserver(TrendRelevanceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        if (!strategy.isRelevant(trend)) {
            System.out.println("  [Observer] Production Dept filtered out '" + trend.getName() + "' (not relevant).");
            return;
        }
        TrendAlert alert = new TrendAlert(
            FileManager.nextId(ALERTS_FILE),
            trend.getId(),
            DEPARTMENT,
            strategy.priorityFor(trend),
            LocalDate.now().toString(),
            false,
            strategy.buildMessage(trend)
        );
        FileManager.appendLine(ALERTS_FILE, alert.toCSV());
        System.out.println("  [Observer] Production Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
