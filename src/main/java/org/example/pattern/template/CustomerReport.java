package org.example.pattern.template;

/**
 * Template Method pattern — abstract base for Customer Relations reports.
 *
 * generate() defines the fixed skeleton: loadData → printHeader → printBody → printFooter.
 * It is declared final so no subclass can reorder or skip steps.
 *
 * Subclasses must implement loadData(), printHeader(), and printBody().
 * printFooter() is a hook — it does nothing by default; subclasses may override it.
 *
 * Concrete subclasses:
 *   ComplaintHistoryReport  — per-customer complaint + audit trail view
 *   CustomerSummaryReport   — cross-customer aggregate statistics
 */
public abstract class CustomerReport {

    /** Runs the report. Steps are fixed; only their content varies per subclass. */
    public final void generate() {
        loadData();
        printHeader();
        printBody();
        printFooter();
    }

    /** Load whatever CSV data this report needs. */
    protected abstract void loadData();

    /** Print the report title box. */
    protected abstract void printHeader();

    /** Print the main report content. */
    protected abstract void printBody();

    /** Print summary totals or closing lines. Default: nothing. */
    protected void printFooter() {}
}
