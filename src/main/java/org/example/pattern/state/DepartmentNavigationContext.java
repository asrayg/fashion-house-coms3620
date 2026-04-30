package org.example.pattern.state;

import java.util.Scanner;

/**
 * Context for the Department-switching State pattern.
 *
 * Holds the currently active DepartmentState and forwards the menu
 * loop to it. Calling transitionTo(...) is what "switching departments"
 * means in this codebase.
 *
 * This is a stub — only Design and Partnership states are wired today.
 * Production, Marketing, Sales & Retail, Finance, and Administration
 * states are pending; see the iteration-3 State Pattern report for the
 * effort estimate.
 */
public class DepartmentNavigationContext {

    private DepartmentState current;

    public void transitionTo(DepartmentState newState) {
        this.current = newState;
    }

    public DepartmentState current() {
        return current;
    }

    public void run(Scanner scanner) {
        if (current == null) {
            throw new IllegalStateException("No department state set; call transitionTo first.");
        }
        System.out.println("\n>> Entering " + current.label() + " <<");
        current.enter(scanner);
        System.out.println(">> Leaving " + current.label() + " <<");
    }
}
