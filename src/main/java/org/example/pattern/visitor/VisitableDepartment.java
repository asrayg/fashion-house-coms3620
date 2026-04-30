package org.example.pattern.visitor;

/**
 * Element interface for the Visitor pattern.
 * Every department class implements this and forwards the visitor
 * to the visit overload that matches its concrete type (double dispatch).
 */
public interface VisitableDepartment {
    void accept(DepartmentVisitor visitor);
}
