package org.example.pattern.state;

import java.util.Scanner;

/**
 * State pattern — Department navigation.
 *
 * Each concrete state represents one department the user is "inside".
 * The context (DepartmentNavigationContext) holds the active state and
 * delegates the menu loop to it. Switching departments == swapping states.
 *
 * Scope is intentionally narrow: this is for top-level menu navigation
 * only, not for the per-department lifecycle (PLANNING/ACTIVE/CLOSED).
 */
public interface DepartmentState {

    /** Human-readable name shown in the navigation header. */
    String label();

    /** Run the department's menu loop until the user backs out. */
    void enter(Scanner scanner);
}
