package org.example.strategy;

import org.example.model.MarketTrend;
import org.example.model.TrendAlert;

/**
 * Production only acts on trends that materially affect manufacturing —
 * FABRIC and CONSUMER_BEHAVIOR (demand) signals. Both can change
 * batch volumes or sourcing. Confidence ≥3 is required to avoid noise.
 */
public class ProductionRelevanceStrategy implements TrendRelevanceStrategy {

    @Override
    public boolean isRelevant(MarketTrend trend) {
        boolean material   = trend.getCategory() == MarketTrend.Category.FABRIC;
        boolean demand     = trend.getCategory() == MarketTrend.Category.CONSUMER_BEHAVIOR;
        return (material || demand) && trend.getConfidenceLevel() >= 3;
    }

    @Override
    public TrendAlert.Priority priorityFor(MarketTrend trend) {
        if (trend.getLifecycleStage() == MarketTrend.LifecycleStage.PEAK
         || trend.getLifecycleStage() == MarketTrend.LifecycleStage.RISING) {
            return TrendAlert.Priority.HIGH;
        }
        return TrendAlert.Priority.MEDIUM;
    }

    @Override
    public String buildMessage(MarketTrend trend) {
        return "Production alert: " + trend.getCategory() + " trend '"
             + trend.getName() + "' (confidence " + trend.getConfidenceLevel()
             + "/5) may affect batch sizing or material sourcing for "
             + trend.getSeason() + ".";
    }
}
