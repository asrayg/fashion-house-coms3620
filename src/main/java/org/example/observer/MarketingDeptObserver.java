package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.strategy.MarketingRelevanceStrategy;
import org.example.strategy.TrendRelevanceStrategy;
import org.example.util.FileManager;

import java.time.LocalDate;

/**
 * Observer for the Marketing & Communications department.
 * Composes a TrendRelevanceStrategy so the policy that decides which
 * trends generate alerts is replaceable at runtime.
 */
public class MarketingDeptObserver implements TrendObserver {

    private static final String DEPARTMENT  = "Marketing";
    private static final String ALERTS_FILE = "data/research/trend_alerts.csv";

    private final TrendRelevanceStrategy strategy;

    public MarketingDeptObserver() {
        this(new MarketingRelevanceStrategy());
    }

    public MarketingDeptObserver(TrendRelevanceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        if (!strategy.isRelevant(trend)) {
            System.out.println("  [Observer] Marketing Dept filtered out '" + trend.getName() + "' (not relevant).");
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
        System.out.println("  [Observer] Marketing Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
