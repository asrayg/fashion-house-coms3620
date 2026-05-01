package org.example.pattern.visitor;

import org.example.model.DesignDepartment;
import org.example.model.PartnershipDepartment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Reports a per-department capacity utilization line.
 * The current load is supplied by the caller via two lookup functions
 * so this visitor stays decoupled from controllers / file paths.
 */
public class CapacityUtilizationVisitor implements DepartmentVisitor {

    private final Function<Integer, Integer> designLoadLookup;
    private final Function<Integer, Integer> partnershipLoadLookup;
    private final List<String> lines = new ArrayList<>();

    public CapacityUtilizationVisitor(Function<Integer, Integer> designLoadLookup,
                                      Function<Integer, Integer> partnershipLoadLookup) {
        this.designLoadLookup = designLoadLookup;
        this.partnershipLoadLookup = partnershipLoadLookup;
    }

    @Override
    public void visit(DesignDepartment department) {
        int load = designLoadLookup.apply(department.getId());
        lines.add(format("Design", department.getName(), load, department.getMaxCapacity()));
    }

    @Override
    public void visit(PartnershipDepartment department) {
        int load = partnershipLoadLookup.apply(department.getId());
        lines.add(format("Partnership", department.getName(), load, department.getMaxPartners()));
    }

    public List<String> getReportLines() {
        return lines;
    }

    private static String format(String type, String name, int load, int max) {
        double pct = (max > 0) ? (load * 100.0 / max) : 0.0;
        return "  [" + type + "] " + name + " — " + load + "/" + max
             + " (" + String.format("%.0f", pct) + "%)";
    }
}
