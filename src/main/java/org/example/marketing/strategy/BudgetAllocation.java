package org.example.marketing.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Strategy pattern for campaign budget allocation: one place for the interface,
 * context, and concrete strategies (even split vs per-platform input).
 *
 * <p>Callers build a {@link Context} with a {@link Strategy} at runtime; the
 * context delegates and does not branch on algorithm.</p>
 *
 * Strategy Pattern — Iteration 3
 * Code Owner: Anoop Boyal
 */
public final class BudgetAllocation {

    private BudgetAllocation() {}

    // -------------------------------------------------------------------------
    // Strategy interface
    // -------------------------------------------------------------------------

    /**
     * Interchangeable algorithm for distributing budget across platform names.
     */
    public interface Strategy {

        /**
         * @param totalBudget used by {@link EvenSplit}; ignored for per-line entry in {@link PerPlatform}
         * @return amounts in platform order, or null if input invalid
         */
        List<Double> allocate(List<String> platforms, double totalBudget, Scanner scanner);

        default double sumBudgets(List<Double> budgets) {
            return budgets.stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    // -------------------------------------------------------------------------
    // Context
    // -------------------------------------------------------------------------

    /**
     * Holds the active {@link Strategy} and delegates allocation.
     */
    public static final class Context {

        private Strategy strategy;

        public Context(Strategy strategy) {
            this.strategy = strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public List<Double> allocate(List<String> platforms, double totalBudget, Scanner scanner) {
            return strategy.allocate(platforms, totalBudget, scanner);
        }

        public double sumBudgets(List<Double> budgets) {
            return strategy.sumBudgets(budgets);
        }
    }

    // -------------------------------------------------------------------------
    // Concrete strategies
    // -------------------------------------------------------------------------

    /** Splits total budget evenly across all platforms. */
    public static final class EvenSplit implements Strategy {

        @Override
        public List<Double> allocate(List<String> platforms, double totalBudget, Scanner scanner) {
            if (totalBudget <= 0) return null;

            List<Double> budgets = new ArrayList<>();
            double each = totalBudget / platforms.size();

            for (String ignored : platforms) {
                budgets.add(each);
            }

            System.out.printf("  $%.2f allocated per platform.%n", each);
            return budgets;
        }
    }

    /** Prompts for a positive amount per platform. */
    public static final class PerPlatform implements Strategy {

        @Override
        public List<Double> allocate(List<String> platforms, double totalBudget, Scanner scanner) {
            List<Double> budgets = new ArrayList<>();

            for (String platform : platforms) {
                System.out.print("Budget for " + platform + " ($): ");
                try {
                    double b = Double.parseDouble(scanner.nextLine().trim());
                    if (b <= 0) {
                        System.out.println("Error: Budget for "
                                + platform + " must be positive.");
                        return null;
                    }
                    budgets.add(b);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid budget for "
                            + platform + ".");
                    return null;
                }
            }

            return budgets;
        }
    }
}
