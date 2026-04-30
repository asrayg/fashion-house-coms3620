package org.example.pattern;

import org.example.model.ComplianceViolation;

/**
 * Observer interface for the Observer pattern.
 * Departments can implement this to receive compliance violation notifications.
 * Used by the Legal & Compliance department to notify interested parties.
 */
public interface ComplianceObserver {
    /**
     * Called when a new compliance violation is recorded.
     * 
     * @param violation the compliance violation that was recorded
     */
    void onViolationRecorded(ComplianceViolation violation);

    /**
     * Called when a violation status changes.
     * 
     * @param violation the compliance violation with updated status
     */
    void onViolationStatusChanged(ComplianceViolation violation);

    /**
     * Get the name of this observer (e.g., the department name).
     * 
     * @return the observer name
     */
    String getObserverName();
}
