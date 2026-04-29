package org.example.observer;

import org.example.model.MarketTrend;

/**
 * Observer side of the Observer pattern. Implementations are registered
 * with a TrendSubject (ResearchTrendController) and notified each time
 * a new MarketTrend is logged.
 *
 * Each observer also exposes its department name so the subject can
 * report who was notified, and so observers can be looked up later for
 * subscribe/unsubscribe operations.
 */
public interface TrendObserver {

    /** Department display name (e.g. "Design", "Marketing", "Production"). */
    String getDepartmentName();

    /** Called by the subject after a MarketTrend has been persisted. */
    void onTrendLogged(MarketTrend trend);
}
