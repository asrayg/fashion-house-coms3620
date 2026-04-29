package org.example.strategy;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;

/**
 * Strategy interface (GoF Strategy pattern) used by every TrendObserver
 * to decide (a) whether a given MarketTrend warrants an alert for the
 * department it represents and (b) what the alert text and priority should be.
 *
 * Swapping strategies lets us change a department's filtering and messaging
 * policy without touching the Observer or Subject code.
 */
public interface TrendRelevanceStrategy {

    /** True if this department should be alerted about the given trend. */
    boolean isRelevant(MarketTrend trend);

    /** Compute the alert priority for the trend (LOW, MEDIUM, HIGH). */
    TrendAlert.Priority priorityFor(MarketTrend trend);

    /** Build the human-readable alert message for the department. */
    String buildMessage(MarketTrend trend);
}
