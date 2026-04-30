package org.example.pattern;

import org.example.model.ComplianceViolation;
import java.util.ArrayList;
import java.util.List;

/**
 * Observable/Subject for the Observer pattern.
 * The Legal & Compliance department uses this to notify observers (other departments)
 * about violations and status changes.
 */
public class ComplianceNotificationCenter {
    private static final List<ComplianceObserver> observers = new ArrayList<>();

    /**
     * Register an observer to receive compliance notifications.
     * 
     * @param observer the observer to register
     */
    public static void attach(ComplianceObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("* " + observer.getObserverName() + " has subscribed to compliance notifications.");
        }
    }

    /**
     * Unregister an observer.
     * 
     * @param observer the observer to unregister
     */
    public static void detach(ComplianceObserver observer) {
        if (observers.remove(observer)) {
            System.out.println("* " + observer.getObserverName() + " has unsubscribed from compliance notifications.");
        }
    }

    /**
     * Notify all observers of a new violation.
     * 
     * @param violation the new compliance violation
     */
    public static void notifyViolationRecorded(ComplianceViolation violation) {
        System.out.println("\n[COMPLIANCE ALERT] Recording violation: " + violation.getViolationType());
        for (ComplianceObserver observer : observers) {
            observer.onViolationRecorded(violation);
        }
    }

    /**
     * Notify all observers of a violation status change.
     * 
     * @param violation the violation with updated status
     */
    public static void notifyViolationStatusChanged(ComplianceViolation violation) {
        System.out.println("\n[COMPLIANCE UPDATE] Violation " + violation.getId() + " status changed to: " + violation.getStatus());
        for (ComplianceObserver observer : observers) {
            observer.onViolationStatusChanged(violation);
        }
    }

    /**
     * Get count of registered observers.
     * 
     * @return number of observers
     */
    public static int getObserverCount() {
        return observers.size();
    }

    /**
     * Get list of observer names.
     * 
     * @return list of observer names
     */
    public static List<String> getObserverNames() {
        List<String> names = new ArrayList<>();
        for (ComplianceObserver observer : observers) {
            names.add(observer.getObserverName());
        }
        return names;
    }

    /**
     * Clear all observers (for testing or reset).
     */
    public static void clearObservers() {
        observers.clear();
    }
}
