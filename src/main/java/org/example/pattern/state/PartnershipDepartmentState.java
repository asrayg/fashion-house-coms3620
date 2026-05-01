package org.example.pattern.state;

import org.example.controller.PartnershipDepartmentController;

import java.util.Scanner;

/**
 * Concrete state: the user is navigating inside the
 * Wholesale &amp; Retail Partnerships department.
 */
public class PartnershipDepartmentState implements DepartmentState {

    @Override
    public String label() {
        return "Wholesale & Retail Partnerships";
    }

    @Override
    public void enter(Scanner scanner) {
        new PartnershipDepartmentController(scanner).menu();
    }
}
