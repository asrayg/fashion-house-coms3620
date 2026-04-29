package org.example.strategy;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;

/**
 * Design dept cares about FASHION, COLOR, FABRIC, SILHOUETTE trends —
 * those that translate directly into design decisions. Skips pure
 * CONSUMER_BEHAVIOR signals. Priority scales with confidence.
 */
public class DesignRelevanceStrategy implements TrendRelevanceStrategy {

    @Override
    public boolean isRelevant(MarketTrend trend) {
        switch (trend.getCategory()) {
            case FASHION:
            case COLOR:
            case FABRIC:
            case SILHOUETTE:
                return trend.getConfidenceLevel() >= 2;
            default:
                return false;
        }
    }

    @Override
    public TrendAlert.Priority priorityFor(MarketTrend trend) {
        if (trend.getConfidenceLevel() >= 4) return TrendAlert.Priority.HIGH;
        if (trend.getConfidenceLevel() >= 3) return TrendAlert.Priority.MEDIUM;
        return TrendAlert.Priority.LOW;
    }

    @Override
    public String buildMessage(MarketTrend trend) {
        return "Design alert: new " + trend.getCategory() + " trend '"
             + trend.getName() + "' (lifecycle " + trend.getLifecycleStage()
             + ", confidence " + trend.getConfidenceLevel() + "/5) for "
             + trend.getSeason() + ". Consider adjusting upcoming designs.";
    }
}
