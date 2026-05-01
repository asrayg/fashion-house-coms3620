package org.example.observer;

import org.example.model.MarketTrend;

/**
 * Subject side of the Observer pattern. Maintains a collection of
 * TrendObserver instances and notifies them when a MarketTrend is logged.
 * ResearchTrendController is the single concrete subject.
 */
public interface TrendSubject {

    void addObserver(TrendObserver observer);

    void removeObserver(TrendObserver observer);

    /** Fire onTrendLogged on every registered observer in order. */
    void notifyObservers(MarketTrend trend);
}
