package org.example.pattern.visitor;

import org.example.model.DesignDepartment;
import org.example.model.PartnershipDepartment;

/**
 * Visitor pattern entry point for any operation that must walk the
 * heterogeneous set of department classes (Design, Partnership, ...)
 * without bloating each model with auxiliary reporting logic.
 *
 * Add a new visit method here when a new department type is introduced.
 */
public interface DepartmentVisitor {
    void visit(DesignDepartment department);
    void visit(PartnershipDepartment department);
}
