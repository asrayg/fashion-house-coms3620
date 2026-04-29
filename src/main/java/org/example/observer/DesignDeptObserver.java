package org.example.observer;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.strategy.DesignRelevanceStrategy;
import org.example.strategy.TrendRelevanceStrategy;
import org.example.util.FileManager;

import java.time.LocalDate;

/**
 * Observer for the Design department.
 * Composes a TrendRelevanceStrategy (Strategy pattern) so its filtering
 * and message-building rules can be swapped without changing this class.
 */
public class DesignDeptObserver implements TrendObserver {

    private static final String DEPARTMENT  = "Design";
    private static final String ALERTS_FILE = "data/research/trend_alerts.csv";

    private final TrendRelevanceStrategy strategy;

    public DesignDeptObserver() {
        this(new DesignRelevanceStrategy());
    }

    public DesignDeptObserver(TrendRelevanceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String getDepartmentName() {
        return DEPARTMENT;
    }

    @Override
    public void onTrendLogged(MarketTrend trend) {
        if (!strategy.isRelevant(trend)) {
            System.out.println("  [Observer] Design Dept filtered out '" + trend.getName() + "' (not relevant).");
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
        System.out.println("  [Observer] Design Dept notified ("
                + alert.getPriority() + "): " + trend.getName());
    }
}
