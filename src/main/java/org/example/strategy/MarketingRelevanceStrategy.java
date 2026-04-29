package org.example.strategy;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;

/**
 * Marketing wants every trend except FADED ones, because even niche
 * signals can inform creative messaging. Priority skews HIGH for
 * trends already at PEAK or RISING with high confidence.
 */
public class MarketingRelevanceStrategy implements TrendRelevanceStrategy {

    @Override
    public boolean isRelevant(MarketTrend trend) {
        return trend.getLifecycleStage() != MarketTrend.LifecycleStage.FADED;
    }

    @Override
    public TrendAlert.Priority priorityFor(MarketTrend trend) {
        boolean strongLifecycle =
            trend.getLifecycleStage() == MarketTrend.LifecycleStage.PEAK
         || trend.getLifecycleStage() == MarketTrend.LifecycleStage.RISING;

        if (strongLifecycle && trend.getConfidenceLevel() >= 4) return TrendAlert.Priority.HIGH;
        if (trend.getConfidenceLevel() >= 3)                    return TrendAlert.Priority.MEDIUM;
        return TrendAlert.Priority.LOW;
    }

    @Override
    public String buildMessage(MarketTrend trend) {
        return "Marketing alert: trend '" + trend.getName()
             + "' (source: " + trend.getSource()
             + ", region: " + trend.getTargetRegion()
             + ") may inform campaign targeting for " + trend.getSeason() + ".";
    }
}
