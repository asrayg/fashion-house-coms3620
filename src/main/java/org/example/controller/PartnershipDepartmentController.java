package org.example.controller;

import org.example.model.ContractAdjustment;
import org.example.model.DesignDepartment;
import org.example.model.PartnerEvaluation;
import org.example.model.PartnershipDepartment;
import org.example.model.WholesalePartner;
import org.example.pattern.visitor.BudgetSummaryVisitor;
import org.example.pattern.visitor.CapacityUtilizationVisitor;
import org.example.pattern.visitor.VisitableDepartment;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wholesale &amp; Retail Partnerships — Iteration 3
 * Actor: Partnerships Manager
 * Code Owner: Asray Gopa
 *
 * Use case "Onboard Wholesale Partner and Conduct Compliance Evaluation"
 * passes the boss test because partnerships drive the wholesale revenue loop.
 *
 * Features:
 *  - Create / manage regional partnership departments (budget, channel, capacity)
 *  - Register wholesale partners (boutiques, dept stores, distributors, online retailers)
 *  - Submit partners for compliance + commercial evaluation
 *  - Multi-stage workflow (PENDING → IN_REVIEW → APPROVED / REJECTED / REVISION_NEEDED)
 *  - Score partners on 5 weighted criteria
 *  - Auto-decision against approval / revision thresholds
 *  - Track contract adjustments with budget impact
 *  - Cross-department transfer
 *  - Workload + capacity dashboards (Visitor pattern)
 *  - Performance dashboard and full audit trail
 */
public class PartnershipDepartmentController {

    static final String DEPT_FILE       = "data/partnerships/partnership_departments.csv";
    static final String PARTNER_FILE    = "data/partnerships/wholesale_partners.csv";
    static final String EVALUATION_FILE = "data/partnerships/partner_evaluations.csv";
    static final String ADJUSTMENT_FILE = "data/partnerships/contract_adjustments.csv";
    static final String ASSIGN_FILE     = "data/partnerships/department_employee_assignments.csv";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final double WEIGHT_FINANCIAL_STABILITY  = 0.25;
    private static final double WEIGHT_BRAND_ALIGNMENT      = 0.15;
    private static final double WEIGHT_SALES_POTENTIAL      = 0.25;
    private static final double WEIGHT_DISTRIBUTION_REACH   = 0.20;
    private static final double WEIGHT_PAYMENT_RELIABILITY  = 0.15;

    private static final double APPROVAL_THRESHOLD     = 7.0;
    private static final double REVISION_THRESHOLD     = 5.0;
    private static final double EVALUATION_COST        = 200.00;
    private static final double LOW_BUDGET_RATIO       = 0.10;

    private final Scanner scanner;

    public PartnershipDepartmentController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║   Wholesale & Retail Partnerships Mgmt      ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Create Partnership Department          ║");
            System.out.println("║  2.  List All Departments                   ║");
            System.out.println("║  3.  View Department Details                ║");
            System.out.println("║  4.  Update Department Budget               ║");
            System.out.println("║  5.  Change Department Status               ║");
            System.out.println("║  6.  Register Wholesale Partner             ║");
            System.out.println("║  7.  Submit Partner for Evaluation          ║");
            System.out.println("║  8.  Conduct Partner Evaluation (Score)     ║");
            System.out.println("║  9.  View Evaluation Pipeline               ║");
            System.out.println("║ 10.  Record Contract Adjustment             ║");
            System.out.println("║ 11.  View Adjustment History                ║");
            System.out.println("║ 12.  Transfer Partner Between Departments   ║");
            System.out.println("║ 13.  Cross-Dept Visitor Dashboard           ║");
            System.out.println("║ 14.  Evaluation Audit Trail                 ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> createDepartment();
                case "2"  -> listDepartments();
                case "3"  -> viewDepartmentDetails();
                case "4"  -> updateDepartmentBudget();
                case "5"  -> changeDepartmentStatus();
                case "6"  -> registerWholesalePartner();
                case "7"  -> submitPartnerForEvaluation();
                case "8"  -> conductPartnerEvaluation();
                case "9"  -> viewEvaluationPipeline();
                case "10" -> recordContractAdjustment();
                case "11" -> viewAdjustmentHistory();
                case "12" -> transferPartner();
                case "13" -> crossDepartmentVisitorDashboard();
                case "14" -> evaluationAuditTrail();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Create Partnership Department
    // =========================================================================

    private void createDepartment() {
        System.out.println("\n--- Create Partnership Department ---");

        String name           = readNonEmpty("Department name: ");
        String accountManager = readNonEmpty("Account manager name: ");
        String region         = readNonEmpty("Region (e.g. North America, EMEA, APAC): ");
        String channel        = readNonEmpty("Channel (Boutique/DepartmentStore/OnlineRetailer/Distributor): ");
        if (name == null || accountManager == null || region == null || channel == null) return;

        Double budget = readPositiveDouble("Department budget ($): ");
        if (budget == null) return;

        Integer maxPartners = readPositiveInt("Max partners this department can manage: ");
        if (maxPartners == null) return;

        for (PartnershipDepartment existing : loadAllDepartments()) {
            if (existing.getName().equalsIgnoreCase(name) && existing.getRegion().equalsIgnoreCase(region)) {
                System.out.println("Error: A department with that name already exists for this region.");
                return;
            }
        }

        int id = FileManager.nextId(DEPT_FILE);
        PartnershipDepartment dept = new PartnershipDepartment(id, name, accountManager, budget, 0.0,
                                                                region, PartnershipDepartment.Status.ACTIVE,
                                                                channel, maxPartners);
        FileManager.appendLine(DEPT_FILE, dept.toCSV());
        System.out.println("Department created successfully: " + dept);
    }

    // =========================================================================
    // 2. List All Departments
    // =========================================================================

    private void listDepartments() {
        List<PartnershipDepartment> depts = loadAllDepartments();
        if (depts.isEmpty()) {
            System.out.println("No partnership departments on file.");
            return;
        }
        System.out.println("\n--- Partnership Departments ---");
        for (PartnershipDepartment dept : depts) {
            int load = countActivePartners(dept.getId());
            double utilization = (dept.getMaxPartners() > 0)
                ? (load * 100.0 / dept.getMaxPartners()) : 0;
            System.out.println(dept + " | Load: " + load + "/" + dept.getMaxPartners()
                             + " (" + String.format("%.0f", utilization) + "%)");
        }
    }

    // =========================================================================
    // 3. View Department Details
    // =========================================================================

    private void viewDepartmentDetails() {
        PartnershipDepartment dept = promptForDepartment();
        if (dept == null) return;

        int currentLoad = countActivePartners(dept.getId());
        List<PartnerEvaluation> evals = loadEvaluationsForDepartment(dept.getId());
        long approved   = evals.stream().filter(e -> e.getStatus() == PartnerEvaluation.Status.APPROVED).count();
        long rejected   = evals.stream().filter(e -> e.getStatus() == PartnerEvaluation.Status.REJECTED).count();
        long pending    = evals.stream().filter(e -> e.getStatus() == PartnerEvaluation.Status.PENDING
                                                  || e.getStatus() == PartnerEvaluation.Status.IN_REVIEW).count();
        long revNeeded  = evals.stream().filter(e -> e.getStatus() == PartnerEvaluation.Status.REVISION_NEEDED).count();
        double avgScore = evals.stream().filter(e -> e.getOverallScore() > 0)
                               .mapToDouble(PartnerEvaluation::getOverallScore).average().orElse(0);

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║          DEPARTMENT DETAIL REPORT            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  ID              : " + dept.getId());
        System.out.println("  Name            : " + dept.getName());
        System.out.println("  Account Manager : " + dept.getAccountManager());
        System.out.println("  Region          : " + dept.getRegion());
        System.out.println("  Channel         : " + dept.getChannel());
        System.out.println("  Status          : " + dept.getStatus());
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  BUDGET:");
        System.out.println("    Total          : $" + String.format("%.2f", dept.getBudget()));
        System.out.println("    Spent          : $" + String.format("%.2f", dept.getSpentBudget()));
        System.out.println("    Remaining      : $" + String.format("%.2f", dept.getRemainingBudget()));
        System.out.println("    Utilization    : " + String.format("%.1f", dept.getBudgetUtilization()) + "%");
        if (dept.getRemainingBudget() < dept.getBudget() * LOW_BUDGET_RATIO) {
            System.out.println("    *** WARNING: Budget nearly exhausted! ***");
        }
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  CAPACITY:");
        System.out.println("    Max Partners   : " + dept.getMaxPartners());
        System.out.println("    Current Load   : " + currentLoad);
        System.out.println("    Available      : " + (dept.getMaxPartners() - currentLoad));
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  EVALUATION SUMMARY:");
        System.out.println("    Total Evals    : " + evals.size());
        System.out.println("    Approved       : " + approved);
        System.out.println("    Rejected       : " + rejected);
        System.out.println("    Revision Needed: " + revNeeded);
        System.out.println("    Pending/Active : " + pending);
        System.out.println("    Avg Score      : " + String.format("%.2f", avgScore) + "/10");

        System.out.println("  ──────────────────────────────────────");
        System.out.println("  PARTNERS IN THIS DEPARTMENT:");
        List<WholesalePartner> partners = loadAllPartners().stream()
            .filter(p -> p.getDepartmentId() == dept.getId())
            .collect(Collectors.toList());
        if (partners.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (WholesalePartner p : partners) {
                System.out.println("    - " + p);
            }
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 4. Update Department Budget
    // =========================================================================

    private void updateDepartmentBudget() {
        PartnershipDepartment dept = promptForDepartment();
        if (dept == null) return;

        System.out.println("Current budget: $" + String.format("%.2f", dept.getBudget())
                         + " | Spent: $" + String.format("%.2f", dept.getSpentBudget()));
        System.out.println("1. Increase budget");
        System.out.println("2. Decrease budget");
        System.out.println("3. Set new budget");
        System.out.print("Select: ");
        String choice = scanner.nextLine().trim();

        Double amount = readPositiveDouble("Amount ($): ");
        if (amount == null) return;

        switch (choice) {
            case "1" -> {
                dept.setBudget(dept.getBudget() + amount);
                System.out.println("Budget increased. New budget: $" + String.format("%.2f", dept.getBudget()));
            }
            case "2" -> {
                if (dept.getBudget() - amount < dept.getSpentBudget()) {
                    System.out.println("Error: Cannot reduce budget below already spent ($"
                                     + String.format("%.2f", dept.getSpentBudget()) + ").");
                    return;
                }
                dept.setBudget(dept.getBudget() - amount);
                System.out.println("Budget decreased. New budget: $" + String.format("%.2f", dept.getBudget()));
            }
            case "3" -> {
                if (amount < dept.getSpentBudget()) {
                    System.out.println("Error: New budget cannot be less than already spent ($"
                                     + String.format("%.2f", dept.getSpentBudget()) + ").");
                    return;
                }
                dept.setBudget(amount);
                System.out.println("Budget set to: $" + String.format("%.2f", dept.getBudget()));
            }
            default -> {
                System.out.println("Invalid option.");
                return;
            }
        }
        updateDepartment(dept);
    }

    // =========================================================================
    // 5. Change Department Status
    // =========================================================================

    private void changeDepartmentStatus() {
        PartnershipDepartment dept = promptForDepartment();
        if (dept == null) return;

        System.out.println("Current status: " + dept.getStatus());
        System.out.println("1. ACTIVE");
        System.out.println("2. PLANNING");
        System.out.println("3. ON_HOLD");
        System.out.println("4. CLOSED");
        System.out.print("New status: ");
        String choice = scanner.nextLine().trim();

        PartnershipDepartment.Status newStatus = switch (choice) {
            case "1" -> PartnershipDepartment.Status.ACTIVE;
            case "2" -> PartnershipDepartment.Status.PLANNING;
            case "3" -> PartnershipDepartment.Status.ON_HOLD;
            case "4" -> confirmCloseDepartment(dept);
            default  -> null;
        };
        if (newStatus == null) {
            System.out.println("Invalid or cancelled.");
            return;
        }

        dept.setStatus(newStatus);
        updateDepartment(dept);
        System.out.println("Department status updated to: " + newStatus);
    }

    private PartnershipDepartment.Status confirmCloseDepartment(PartnershipDepartment dept) {
        long pendingEvals = loadEvaluationsForDepartment(dept.getId()).stream()
            .filter(e -> e.getStatus() == PartnerEvaluation.Status.PENDING
                      || e.getStatus() == PartnerEvaluation.Status.IN_REVIEW).count();
        if (pendingEvals > 0) {
            System.out.println("Warning: " + pendingEvals
                             + " evaluation(s) still pending in this department.");
            System.out.print("Close anyway? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                return null;
            }
        }
        return PartnershipDepartment.Status.CLOSED;
    }

    // =========================================================================
    // 6. Register Wholesale Partner
    // =========================================================================

    private void registerWholesalePartner() {
        System.out.println("\n--- Register Wholesale Partner ---");

        if (!FileManager.hasRecords(DEPT_FILE)) {
            System.out.println("Error: No partnership departments exist. Create one first.");
            return;
        }

        String name          = readNonEmpty("Partner name: ");
        String partnerType   = readNonEmpty("Type (Boutique/DepartmentStore/OnlineRetailer/Distributor): ");
        String contactPerson = readNonEmpty("Primary contact person: ");
        String contactEmail  = readNonEmpty("Contact email: ");
        String region        = readNonEmpty("Region: ");
        if (name == null || partnerType == null || contactPerson == null
                || contactEmail == null || region == null) return;

        if (loadAllPartners().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
            System.out.println("Error: A partner with that name already exists.");
            return;
        }

        listDepartments();
        Integer deptId = readId("Assign to Department ID: ");
        if (deptId == null) return;

        PartnershipDepartment dept = findDepartmentById(deptId);
        if (dept == null || dept.getStatus() != PartnershipDepartment.Status.ACTIVE) {
            System.out.println("Error: Department not found or not active.");
            return;
        }

        if (countActivePartners(deptId) >= dept.getMaxPartners()) {
            System.out.println("Error: Department is at full capacity.");
            return;
        }

        Double annualVolume = readPositiveDouble("Estimated annual volume ($): ");
        if (annualVolume == null) return;

        int id = FileManager.nextId(PARTNER_FILE);
        WholesalePartner partner = new WholesalePartner(id, name, partnerType, contactPerson,
                                                        contactEmail, region, deptId,
                                                        WholesalePartner.AccountStatus.PROSPECT,
                                                        annualVolume);
        FileManager.appendLine(PARTNER_FILE, partner.toCSV());
        System.out.println("Wholesale partner registered: " + partner);
        System.out.println("Status is PROSPECT until evaluation is approved (option 7 → 8).");
    }

    // =========================================================================
    // 7. Submit Partner for Evaluation
    // =========================================================================

    private void submitPartnerForEvaluation() {
        System.out.println("\n--- Submit Partner for Evaluation ---");

        List<WholesalePartner> partners = loadAllPartners();
        if (partners.isEmpty()) {
            System.out.println("Error: No partners on file. Register one first (option 6).");
            return;
        }

        System.out.println("\n--- Available Partners ---");
        for (WholesalePartner p : partners) {
            System.out.println("  " + p);
        }

        Integer partnerId = readId("\nEnter Partner ID: ");
        if (partnerId == null) return;
        WholesalePartner partner = findPartnerById(partnerId);
        if (partner == null) {
            System.out.println("Error: Partner not found.");
            return;
        }

        PartnershipDepartment dept = findDepartmentById(partner.getDepartmentId());
        if (dept == null || dept.getStatus() != PartnershipDepartment.Status.ACTIVE) {
            System.out.println("Error: Partner's department is not active.");
            return;
        }

        PartnerEvaluation existing = getLatestEvaluationForPartner(partnerId);
        if (existing != null && (existing.getStatus() == PartnerEvaluation.Status.PENDING
                              || existing.getStatus() == PartnerEvaluation.Status.IN_REVIEW)) {
            System.out.println("Error: Partner already has a pending evaluation (#" + existing.getId() + ").");
            return;
        }

        if (dept.getRemainingBudget() < EVALUATION_COST) {
            System.out.println("Error: Insufficient department budget. Required: $"
                             + String.format("%.2f", EVALUATION_COST)
                             + " | Available: $" + String.format("%.2f", dept.getRemainingBudget()));
            return;
        }

        String reviewer = readNonEmpty("Assigned reviewer name: ");
        if (reviewer == null) return;

        int evalId = FileManager.nextId(EVALUATION_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        PartnerEvaluation evaluation = new PartnerEvaluation(evalId, partnerId, dept.getId(), reviewer,
                                                             0, 0, 0, 0, 0, 0,
                                                             PartnerEvaluation.Status.PENDING,
                                                             "Awaiting review", today);
        FileManager.appendLine(EVALUATION_FILE, evaluation.toCSV());

        dept.setSpentBudget(dept.getSpentBudget() + EVALUATION_COST);
        updateDepartment(dept);

        System.out.println("\nPartner submitted for evaluation. Eval ID: " + evalId
                         + " | Reviewer: " + reviewer);
        System.out.println("Evaluation cost of $" + String.format("%.2f", EVALUATION_COST)
                         + " charged to department.");
    }

    // =========================================================================
    // 8. Conduct Partner Evaluation
    // =========================================================================

    private void conductPartnerEvaluation() {
        System.out.println("\n--- Conduct Partner Evaluation ---");

        List<PartnerEvaluation> pending = loadAllEvaluations().stream()
            .filter(e -> e.getStatus() == PartnerEvaluation.Status.PENDING
                      || e.getStatus() == PartnerEvaluation.Status.IN_REVIEW)
            .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("No pending evaluations.");
            return;
        }

        System.out.println("\n--- Pending Evaluations ---");
        for (PartnerEvaluation e : pending) {
            WholesalePartner p = findPartnerById(e.getPartnerId());
            String pName = (p != null) ? p.getName() : "Unknown";
            System.out.println("  Eval #" + e.getId() + " | Partner: " + pName
                             + " [ID:" + e.getPartnerId() + "]"
                             + " | Reviewer: " + e.getReviewerName()
                             + " | Status: " + e.getStatus());
        }

        Integer evalId = readId("\nEnter Evaluation ID to conduct: ");
        if (evalId == null) return;
        PartnerEvaluation evaluation = findEvaluationById(evalId);
        if (evaluation == null) {
            System.out.println("Error: Evaluation not found.");
            return;
        }
        if (evaluation.getStatus() != PartnerEvaluation.Status.PENDING
                && evaluation.getStatus() != PartnerEvaluation.Status.IN_REVIEW) {
            System.out.println("Error: Evaluation is not in a reviewable state.");
            return;
        }

        WholesalePartner partner = findPartnerById(evaluation.getPartnerId());
        if (partner != null) {
            System.out.println("\nEvaluating: " + partner);
        }

        System.out.println("\nScore each criterion (0.0 - 10.0):");
        System.out.println("  Weights: FinancialStability=" + pct(WEIGHT_FINANCIAL_STABILITY)
                         + " BrandAlignment=" + pct(WEIGHT_BRAND_ALIGNMENT)
                         + " SalesPotential=" + pct(WEIGHT_SALES_POTENTIAL)
                         + " DistributionReach=" + pct(WEIGHT_DISTRIBUTION_REACH)
                         + " PaymentReliability=" + pct(WEIGHT_PAYMENT_RELIABILITY));

        Double financial    = readScore("Financial Stability (credit, solvency)");
        if (financial == null) return;
        Double brand        = readScore("Brand Alignment (positioning, target market)");
        if (brand == null) return;
        Double sales        = readScore("Sales Potential (forecast volume)");
        if (sales == null) return;
        Double distribution = readScore("Distribution Reach (geographic / channel coverage)");
        if (distribution == null) return;
        Double payment      = readScore("Payment Reliability (history, terms)");
        if (payment == null) return;

        double overall = (financial * WEIGHT_FINANCIAL_STABILITY)
                       + (brand * WEIGHT_BRAND_ALIGNMENT)
                       + (sales * WEIGHT_SALES_POTENTIAL)
                       + (distribution * WEIGHT_DISTRIBUTION_REACH)
                       + (payment * WEIGHT_PAYMENT_RELIABILITY);

        PartnerEvaluation.Status newStatus;
        String autoFeedback;
        if (overall >= APPROVAL_THRESHOLD) {
            newStatus = PartnerEvaluation.Status.APPROVED;
            autoFeedback = "APPROVED - Meets onboarding standards";
        } else if (overall >= REVISION_THRESHOLD) {
            newStatus = PartnerEvaluation.Status.REVISION_NEEDED;
            autoFeedback = "REVISION NEEDED - Below approval threshold";
        } else {
            newStatus = PartnerEvaluation.Status.REJECTED;
            autoFeedback = "REJECTED - Does not meet minimum standards";
        }

        System.out.print("\nAdditional feedback (optional): ");
        String customFeedback = scanner.nextLine().trim();
        String finalFeedback = customFeedback.isEmpty() ? autoFeedback : autoFeedback + " | " + customFeedback;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║          EVALUATION SCORE SUMMARY            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  Financial Stability  : " + String.format("%.1f", financial)    + "/10  (x" + pct(WEIGHT_FINANCIAL_STABILITY) + ")");
        System.out.println("  Brand Alignment      : " + String.format("%.1f", brand)        + "/10  (x" + pct(WEIGHT_BRAND_ALIGNMENT) + ")");
        System.out.println("  Sales Potential      : " + String.format("%.1f", sales)        + "/10  (x" + pct(WEIGHT_SALES_POTENTIAL) + ")");
        System.out.println("  Distribution Reach   : " + String.format("%.1f", distribution) + "/10  (x" + pct(WEIGHT_DISTRIBUTION_REACH) + ")");
        System.out.println("  Payment Reliability  : " + String.format("%.1f", payment)      + "/10  (x" + pct(WEIGHT_PAYMENT_RELIABILITY) + ")");
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  WEIGHTED OVERALL     : " + String.format("%.2f", overall) + "/10");
        System.out.println("  DECISION             : " + newStatus);
        System.out.println("╚══════════════════════════════════════════════╝");

        System.out.print("\nConfirm evaluation? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Evaluation cancelled.");
            return;
        }

        List<String> lines = FileManager.readLines(EVALUATION_FILE);
        for (int i = 0; i < lines.size(); i++) {
            PartnerEvaluation row = PartnerEvaluation.fromCSV(lines.get(i));
            if (row.getId() == evalId) {
                PartnerEvaluation updated = new PartnerEvaluation(row.getId(), row.getPartnerId(),
                    row.getDepartmentId(), row.getReviewerName(),
                    financial, brand, sales, distribution, payment,
                    overall, newStatus, finalFeedback, LocalDate.now().format(DATE_FMT));
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(EVALUATION_FILE, lines);

        if (newStatus == PartnerEvaluation.Status.APPROVED && partner != null
                && partner.getAccountStatus() != WholesalePartner.AccountStatus.ACTIVE) {
            partner.setAccountStatus(WholesalePartner.AccountStatus.ACTIVE);
            updatePartner(partner);
            System.out.println("Partner '" + partner.getName() + "' is now ACTIVE.");
        }

        System.out.println("Evaluation completed. Status: " + newStatus);
        if (newStatus == PartnerEvaluation.Status.REVISION_NEEDED) {
            System.out.println("Record a contract adjustment (option 10) and resubmit (option 7).");
        }
    }

    // =========================================================================
    // 9. View Evaluation Pipeline
    // =========================================================================

    private void viewEvaluationPipeline() {
        List<PartnerEvaluation> all = loadAllEvaluations();
        if (all.isEmpty()) {
            System.out.println("No evaluations on file.");
            return;
        }

        Map<PartnerEvaluation.Status, List<PartnerEvaluation>> grouped = all.stream()
            .collect(Collectors.groupingBy(PartnerEvaluation::getStatus));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║         PARTNER EVALUATION PIPELINE          ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        for (PartnerEvaluation.Status status : PartnerEvaluation.Status.values()) {
            List<PartnerEvaluation> group = grouped.getOrDefault(status, Collections.emptyList());
            System.out.println("\n  ▸ " + status + " (" + group.size() + ")");
            if (group.isEmpty()) {
                System.out.println("    (none)");
            } else {
                for (PartnerEvaluation e : group) {
                    WholesalePartner p = findPartnerById(e.getPartnerId());
                    String pName = (p != null) ? p.getName() : "Unknown";
                    System.out.println("    Eval #" + e.getId() + " | " + pName
                                     + " | Score: " + String.format("%.1f", e.getOverallScore())
                                     + " | Reviewer: " + e.getReviewerName()
                                     + " | Date: " + e.getEvaluationDate());
                }
            }
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 10. Record Contract Adjustment
    // =========================================================================

    private void recordContractAdjustment() {
        System.out.println("\n--- Record Contract Adjustment ---");

        List<PartnerEvaluation> revisionNeeded = loadAllEvaluations().stream()
            .filter(e -> e.getStatus() == PartnerEvaluation.Status.REVISION_NEEDED)
            .collect(Collectors.toList());

        if (revisionNeeded.isEmpty()) {
            System.out.println("No partners currently need adjustment.");
            return;
        }

        System.out.println("Partners needing adjustment:");
        for (PartnerEvaluation e : revisionNeeded) {
            WholesalePartner p = findPartnerById(e.getPartnerId());
            String pName = (p != null) ? p.getName() : "Unknown";
            System.out.println("  Eval #" + e.getId() + " | Partner: " + pName
                             + " [ID:" + e.getPartnerId() + "]"
                             + " | Score: " + String.format("%.1f", e.getOverallScore())
                             + " | Feedback: " + e.getFeedback());
        }

        Integer partnerId = readId("\nEnter Partner ID to adjust: ");
        if (partnerId == null) return;
        WholesalePartner partner = findPartnerById(partnerId);
        if (partner == null) {
            System.out.println("Error: Partner not found.");
            return;
        }

        Integer evalId = readId("Enter Evaluation ID that triggered this adjustment: ");
        if (evalId == null) return;
        PartnerEvaluation evaluation = findEvaluationById(evalId);
        if (evaluation == null || evaluation.getPartnerId() != partnerId) {
            System.out.println("Error: Evaluation not found or does not match this partner.");
            return;
        }

        int adjNum = nextAdjustmentNumber(partnerId);

        String adjustedBy = readNonEmpty("Adjusted by (manager name): ");
        if (adjustedBy == null) return;

        String changes = readNonEmpty("Describe the contract changes: ");
        if (changes == null) return;

        Double budgetImpact = readNonNegativeDouble("Budget impact ($, 0 if none): ");
        if (budgetImpact == null) return;

        int adjId = FileManager.nextId(ADJUSTMENT_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        ContractAdjustment adjustment = new ContractAdjustment(adjId, partnerId, adjNum, changes,
                                                                adjustedBy, today, evalId, budgetImpact);
        FileManager.appendLine(ADJUSTMENT_FILE, adjustment.toCSV());

        if (budgetImpact > 0) {
            PartnershipDepartment dept = findDepartmentById(evaluation.getDepartmentId());
            if (dept != null) {
                dept.setSpentBudget(dept.getSpentBudget() + budgetImpact);
                updateDepartment(dept);
                System.out.println("$" + String.format("%.2f", budgetImpact)
                                 + " charged to department '" + dept.getName() + "'.");
            }
        }

        System.out.println("\nAdjustment recorded: " + adjustment);
    }

    // =========================================================================
    // 11. View Adjustment History
    // =========================================================================

    private void viewAdjustmentHistory() {
        Integer partnerId = readId("Enter Partner ID (or 0 for all): ");
        if (partnerId == null) return;

        List<String> lines = FileManager.readLines(ADJUSTMENT_FILE);
        if (lines.isEmpty()) {
            System.out.println("No adjustment history on file.");
            return;
        }

        List<ContractAdjustment> adjustments = lines.stream()
            .map(ContractAdjustment::fromCSV)
            .filter(a -> partnerId == 0 || a.getPartnerId() == partnerId)
            .collect(Collectors.toList());

        if (adjustments.isEmpty()) {
            System.out.println("No adjustments found for partner ID " + partnerId + ".");
            return;
        }

        System.out.println("\n--- Contract Adjustment History ---");
        for (ContractAdjustment a : adjustments) {
            WholesalePartner p = findPartnerById(a.getPartnerId());
            String pName = (p != null) ? p.getName() : "Unknown";
            System.out.println("  " + a + " | Partner: " + pName);
        }
        System.out.println("Total adjustments: " + adjustments.size());
        double totalImpact = adjustments.stream().mapToDouble(ContractAdjustment::getBudgetImpact).sum();
        System.out.println("Total budget impact: $" + String.format("%.2f", totalImpact));
    }

    // =========================================================================
    // 12. Transfer Partner Between Departments
    // =========================================================================

    private void transferPartner() {
        System.out.println("\n--- Transfer Partner Between Departments ---");

        Integer partnerId = readId("Enter Partner ID: ");
        if (partnerId == null) return;
        WholesalePartner partner = findPartnerById(partnerId);
        if (partner == null) {
            System.out.println("Error: Partner not found.");
            return;
        }

        PartnershipDepartment source = findDepartmentById(partner.getDepartmentId());
        if (source == null) {
            System.out.println("Error: Source department not found.");
            return;
        }

        Integer destId = readId("Enter destination Department ID: ");
        if (destId == null) return;
        if (destId == source.getId()) {
            System.out.println("Error: Source and destination are the same.");
            return;
        }

        PartnershipDepartment destination = findDepartmentById(destId);
        if (destination == null || destination.getStatus() != PartnershipDepartment.Status.ACTIVE) {
            System.out.println("Error: Destination department not found or not active.");
            return;
        }

        if (countActivePartners(destId) >= destination.getMaxPartners()) {
            System.out.println("Error: Destination department at capacity.");
            return;
        }

        if (destination.getRemainingBudget() < EVALUATION_COST) {
            System.out.println("Error: Destination department has insufficient budget for re-evaluation.");
            return;
        }

        String reviewer = readNonEmpty("Assigned reviewer in destination department: ");
        if (reviewer == null) return;

        System.out.println("\nTransfer summary:");
        System.out.println("  Partner: " + partner.getName() + " [ID:" + partnerId + "]");
        System.out.println("  From: " + source.getName() + " → To: " + destination.getName());
        System.out.println("  New reviewer: " + reviewer);
        System.out.print("Confirm transfer? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Transfer cancelled.");
            return;
        }

        partner.setDepartmentId(destId);
        updatePartner(partner);

        int evalId = FileManager.nextId(EVALUATION_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        PartnerEvaluation transferEval = new PartnerEvaluation(evalId, partnerId, destId, reviewer,
                                                                0, 0, 0, 0, 0, 0,
                                                                PartnerEvaluation.Status.PENDING,
                                                                "Transferred from " + source.getName(),
                                                                today);
        FileManager.appendLine(EVALUATION_FILE, transferEval.toCSV());

        destination.setSpentBudget(destination.getSpentBudget() + EVALUATION_COST);
        updateDepartment(destination);

        System.out.println("Partner transferred. New evaluation ID: " + evalId);
    }

    // =========================================================================
    // 13. Cross-Department Visitor Dashboard (Visitor pattern in action)
    // =========================================================================

    private void crossDepartmentVisitorDashboard() {
        List<VisitableDepartment> allDepartments = collectAllVisitableDepartments();
        if (allDepartments.isEmpty()) {
            System.out.println("No departments on file.");
            return;
        }

        BudgetSummaryVisitor budgetVisitor = new BudgetSummaryVisitor();
        CapacityUtilizationVisitor capacityVisitor = new CapacityUtilizationVisitor(
            this::countDesignDepartmentLoad,
            this::countActivePartners
        );

        for (VisitableDepartment dept : allDepartments) {
            dept.accept(budgetVisitor);
            dept.accept(capacityVisitor);
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║      CROSS-DEPARTMENT VISITOR DASHBOARD              ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("  BUDGET (BudgetSummaryVisitor):");
        System.out.println("    " + budgetVisitor.summary());
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  CAPACITY (CapacityUtilizationVisitor):");
        for (String line : capacityVisitor.getReportLines()) {
            System.out.println(line);
        }
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    private List<VisitableDepartment> collectAllVisitableDepartments() {
        List<VisitableDepartment> all = new ArrayList<>();
        for (String line : FileManager.readLines(DesignDepartmentController.DEPT_FILE)) {
            all.add(DesignDepartment.fromCSV(line));
        }
        all.addAll(loadAllDepartments());
        return all;
    }

    private int countDesignDepartmentLoad(int designDeptId) {
        int count = 0;
        Set<Integer> seen = new java.util.HashSet<>();
        for (String line : FileManager.readLines(DesignDepartmentController.REVIEW_FILE)) {
            org.example.model.DesignReview r = org.example.model.DesignReview.fromCSV(line);
            if (r.getDepartmentId() == designDeptId
                    && r.getStatus() != org.example.model.DesignReview.Status.REJECTED
                    && seen.add(r.getGarmentDesignId())) {
                count++;
            }
        }
        return count;
    }

    // =========================================================================
    // 14. Evaluation Audit Trail
    // =========================================================================

    private void evaluationAuditTrail() {
        Integer partnerId = readId("Enter Partner ID (or 0 for all): ");
        if (partnerId == null) return;

        List<PartnerEvaluation> evals = loadAllEvaluations();
        List<String> adjLines = FileManager.readLines(ADJUSTMENT_FILE);

        if (partnerId > 0) {
            evals = evals.stream().filter(e -> e.getPartnerId() == partnerId)
                                  .collect(Collectors.toList());
        }

        if (evals.isEmpty()) {
            System.out.println("No audit trail found.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         PARTNER EVALUATION AUDIT TRAIL           ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        Map<Integer, List<PartnerEvaluation>> byPartner = evals.stream()
            .collect(Collectors.groupingBy(PartnerEvaluation::getPartnerId, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<PartnerEvaluation>> entry : byPartner.entrySet()) {
            WholesalePartner p = findPartnerById(entry.getKey());
            String pName = (p != null) ? p.getName() : "Unknown";

            System.out.println("\n  Partner: " + pName + " [ID:" + entry.getKey() + "]");
            System.out.println("  ────────────────────────────────────────");

            List<PartnerEvaluation> partnerEvals = entry.getValue();
            partnerEvals.sort(Comparator.comparing(PartnerEvaluation::getEvaluationDate));

            for (PartnerEvaluation e : partnerEvals) {
                PartnershipDepartment dept = findDepartmentById(e.getDepartmentId());
                String deptName = (dept != null) ? dept.getName() : "Unknown";
                System.out.println("    [" + e.getEvaluationDate() + "] Eval #" + e.getId()
                                 + " | Dept: " + deptName + " | Reviewer: " + e.getReviewerName()
                                 + " | Score: " + String.format("%.1f", e.getOverallScore())
                                 + " | " + e.getStatus());
                if (!e.getFeedback().equals("Awaiting review")) {
                    System.out.println("      Feedback: " + e.getFeedback());
                }

                for (String al : adjLines) {
                    ContractAdjustment a = ContractAdjustment.fromCSV(al);
                    if (a.getPreviousEvaluationId() == e.getId()) {
                        System.out.println("      → Adjustment #" + a.getAdjustmentNumber()
                                         + " by " + a.getAdjustedBy()
                                         + " on " + a.getAdjustmentDate()
                                         + " | Changes: " + a.getChangeDescription()
                                         + " | Cost: $" + String.format("%.2f", a.getBudgetImpact()));
                    }
                }
            }
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // Repository helpers — single source of truth for I/O
    // =========================================================================

    private List<PartnershipDepartment> loadAllDepartments() {
        return FileManager.readLines(DEPT_FILE).stream()
            .map(PartnershipDepartment::fromCSV)
            .collect(Collectors.toList());
    }

    private List<WholesalePartner> loadAllPartners() {
        return FileManager.readLines(PARTNER_FILE).stream()
            .map(WholesalePartner::fromCSV)
            .collect(Collectors.toList());
    }

    private List<PartnerEvaluation> loadAllEvaluations() {
        return FileManager.readLines(EVALUATION_FILE).stream()
            .map(PartnerEvaluation::fromCSV)
            .collect(Collectors.toList());
    }

    private List<PartnerEvaluation> loadEvaluationsForDepartment(int deptId) {
        return loadAllEvaluations().stream()
            .filter(e -> e.getDepartmentId() == deptId)
            .collect(Collectors.toList());
    }

    private PartnershipDepartment findDepartmentById(int id) {
        for (PartnershipDepartment d : loadAllDepartments()) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    private WholesalePartner findPartnerById(int id) {
        for (WholesalePartner p : loadAllPartners()) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private PartnerEvaluation findEvaluationById(int id) {
        for (PartnerEvaluation e : loadAllEvaluations()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    private void updateDepartment(PartnershipDepartment updated) {
        rewriteRow(DEPT_FILE, line -> PartnershipDepartment.fromCSV(line).getId() == updated.getId(),
                   updated.toCSV());
    }

    private void updatePartner(WholesalePartner updated) {
        rewriteRow(PARTNER_FILE, line -> WholesalePartner.fromCSV(line).getId() == updated.getId(),
                   updated.toCSV());
    }

    private void rewriteRow(String filePath, java.util.function.Predicate<String> match, String newCsv) {
        List<String> lines = FileManager.readLines(filePath);
        for (int i = 0; i < lines.size(); i++) {
            if (match.test(lines.get(i))) {
                lines.set(i, newCsv);
                break;
            }
        }
        FileManager.writeLines(filePath, lines);
    }

    private int countActivePartners(int deptId) {
        return (int) loadAllPartners().stream()
            .filter(p -> p.getDepartmentId() == deptId)
            .filter(p -> p.getAccountStatus() != WholesalePartner.AccountStatus.TERMINATED)
            .count();
    }

    private PartnerEvaluation getLatestEvaluationForPartner(int partnerId) {
        PartnerEvaluation latest = null;
        for (PartnerEvaluation e : loadAllEvaluations()) {
            if (e.getPartnerId() == partnerId
                    && (latest == null || e.getId() > latest.getId())) {
                latest = e;
            }
        }
        return latest;
    }

    private int nextAdjustmentNumber(int partnerId) {
        int max = 0;
        for (String line : FileManager.readLines(ADJUSTMENT_FILE)) {
            ContractAdjustment a = ContractAdjustment.fromCSV(line);
            if (a.getPartnerId() == partnerId && a.getAdjustmentNumber() > max) {
                max = a.getAdjustmentNumber();
            }
        }
        return max + 1;
    }

    // =========================================================================
    // Console input helpers
    // =========================================================================

    private PartnershipDepartment promptForDepartment() {
        Integer id = readId("Enter Department ID: ");
        if (id == null) return null;
        PartnershipDepartment dept = findDepartmentById(id);
        if (dept == null) {
            System.out.println("Error: Department not found.");
        }
        return dept;
    }

    private String readNonEmpty(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println("Error: Value is required.");
            return null;
        }
        return value;
    }

    private Integer readId(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return null;
        }
    }

    private Integer readPositiveInt(String prompt) {
        Integer val = readId(prompt);
        if (val == null) return null;
        if (val <= 0) {
            System.out.println("Error: Value must be greater than 0.");
            return null;
        }
        return val;
    }

    private Double readPositiveDouble(String prompt) {
        Double val = readNonNegativeDouble(prompt);
        if (val == null) return null;
        if (val <= 0) {
            System.out.println("Error: Value must be greater than 0.");
            return null;
        }
        return val;
    }

    private Double readNonNegativeDouble(String prompt) {
        System.out.print(prompt);
        try {
            double val = Double.parseDouble(scanner.nextLine().trim());
            if (val < 0) {
                System.out.println("Error: Value cannot be negative.");
                return null;
            }
            return val;
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number.");
            return null;
        }
    }

    private Double readScore(String label) {
        System.out.print("  " + label + " (0-10): ");
        try {
            double score = Double.parseDouble(scanner.nextLine().trim());
            if (score < 0 || score > 10) {
                System.out.println("Error: Score must be between 0 and 10.");
                return null;
            }
            return score;
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid score.");
            return null;
        }
    }

    private static String pct(double weight) {
        return ((int) Math.round(weight * 100)) + "%";
    }
}
