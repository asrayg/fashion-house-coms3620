package org.example.pattern.state;

import org.example.controller.DesignDepartmentController;

import java.util.Scanner;

/**
 * Concrete state: the user is navigating inside the Design Department.
 * Wraps the existing controller without modifying it — Open/Closed.
 */
public class DesignDepartmentState implements DepartmentState {

    @Override
    public String label() {
        return "Design Department";
    }

    @Override
    public void enter(Scanner scanner) {
        new DesignDepartmentController(scanner).menu();
    }
}
