package org.example.controller;

import org.example.model.*;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Design Department Management — Iteration 2
 * Actor: Design Manager
 * Code Owner: Asray Gopa
 *
 * Features:
 *  - Create / manage design departments (budget, specialization, capacity)
 *  - Submit garment designs for formal multi-criteria review
 *  - Multi-stage approval workflow (PENDING → IN_REVIEW → APPROVED/REJECTED/REVISION_NEEDED)
 *  - Score designs on 5 criteria: creativity, feasibility, market fit, cost efficiency, brand alignment
 *  - Auto-calculate weighted overall score with configurable weights
 *  - Track design revisions with budget impact
 *  - Department budget tracking and spend management
 *  - Department workload and capacity analysis
 *  - Cross-department design transfer
 *  - Design approval pipeline view
 *  - Department performance analytics dashboard
 *  - Review history and audit trail
 */
public class DesignDepartmentController {

    static final String DEPT_FILE     = "data/design/design_departments.csv";
    static final String REVIEW_FILE   = "data/design/design_reviews.csv";
    static final String REVISION_FILE = "data/design/design_revisions.csv";
    static final String ASSIGN_FILE   = "data/design/department_employee_assignments.csv";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Weighted scoring criteria (must sum to 1.0)
    private static final double WEIGHT_CREATIVITY      = 0.25;
    private static final double WEIGHT_FEASIBILITY     = 0.20;
    private static final double WEIGHT_MARKET_FIT      = 0.25;
    private static final double WEIGHT_COST_EFFICIENCY = 0.15;
    private static final double WEIGHT_BRAND_ALIGNMENT = 0.15;
    private static final double APPROVAL_THRESHOLD     = 7.0;
    private static final double REVISION_THRESHOLD     = 5.0;
    private static final double REVIEW_COST_PER_DESIGN = 150.00;

    private final Scanner scanner;

    public DesignDepartmentController(Scanner scanner) {
        this.scanner = scanner;
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        Design Department Management         ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Create Design Department               ║");
            System.out.println("║  2.  List All Departments                   ║");
            System.out.println("║  3.  View Department Details                ║");
            System.out.println("║  4.  Update Department Budget               ║");
            System.out.println("║  5.  Change Department Status               ║");
            System.out.println("║  6.  Submit Design for Review               ║");
            System.out.println("║  7.  Conduct Design Review (Score)          ║");
            System.out.println("║  8.  View Review Pipeline                   ║");
            System.out.println("║  9.  Record Design Revision                 ║");
            System.out.println("║ 10.  View Revision History                  ║");
            System.out.println("║ 11.  Transfer Design Between Departments    ║");
            System.out.println("║ 12.  Department Workload Analysis           ║");
            System.out.println("║ 13.  Department Performance Dashboard       ║");
            System.out.println("║ 14.  Review Audit Trail                     ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> createDepartment();
                case "2"  -> listDepartments();
                case "3"  -> viewDepartmentDetails();
                case "4"  -> updateDepartmentBudget();
                case "5"  -> changeDepartmentStatus();
                case "6"  -> submitDesignForReview();
                case "7"  -> conductDesignReview();
                case "8"  -> viewReviewPipeline();
                case "9"  -> recordDesignRevision();
                case "10" -> viewRevisionHistory();
                case "11" -> transferDesign();
                case "12" -> departmentWorkloadAnalysis();
                case "13" -> departmentPerformanceDashboard();
                case "14" -> reviewAuditTrail();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    public void employeeAssignmentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║      Employee-Department Assignments        ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Assign Employee to Department          ║");
            System.out.println("║  2.  View Department Employee Assignments   ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> assignEmployeeToDepartment();
                case "2" -> viewDepartmentEmployeeAssignments();
                case "0" -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Create Design Department
    // =========================================================================

    private void createDepartment() {
        System.out.println("\n--- Create Design Department ---");

        System.out.print("Department name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Head designer name: ");
        String headDesigner = scanner.nextLine().trim();

        System.out.print("Season focus (e.g. Summer 2025): ");
        String season = scanner.nextLine().trim();

        System.out.print("Specialization (e.g. Womenswear/Menswear/Kidswear/Accessories/Couture/Streetwear): ");
        String specialization = scanner.nextLine().trim();

        System.out.print("Department budget ($): ");
        double budget;
        try {
            budget = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Budget must be a valid number.");
            return;
        }
        if (budget <= 0) {
            System.out.println("Error: Budget must be greater than 0.");
            return;
        }

        System.out.print("Max design capacity (number of designs dept can handle): ");
        int maxCapacity;
        try {
            maxCapacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Capacity must be a whole number.");
            return;
        }
        if (maxCapacity <= 0) {
            System.out.println("Error: Capacity must be greater than 0.");
            return;
        }

        if (name.isEmpty() || headDesigner.isEmpty() || season.isEmpty() || specialization.isEmpty()) {
            System.out.println("Error: All fields are required. Department not created.");
            return;
        }

        // Check duplicate name
        for (String line : FileManager.readLines(DEPT_FILE)) {
            DesignDepartment existing = DesignDepartment.fromCSV(line);
            if (existing.getName().equalsIgnoreCase(name) && existing.getSeason().equalsIgnoreCase(season)) {
                System.out.println("Error: A department with that name already exists for this season.");
                return;
            }
        }

        int id = FileManager.nextId(DEPT_FILE);
        DesignDepartment dept = new DesignDepartment(id, name, headDesigner, budget, 0.0,
                                                      season, DesignDepartment.Status.ACTIVE,
                                                      specialization, maxCapacity);
        FileManager.appendLine(DEPT_FILE, dept.toCSV());
        System.out.println("Department created successfully: " + dept);
    }

    // =========================================================================
    // 2. List All Departments
    // =========================================================================

    private void listDepartments() {
        List<String> lines = FileManager.readLines(DEPT_FILE);
        if (lines.isEmpty()) {
            System.out.println("No design departments on file.");
            return;
        }
        System.out.println("\n--- Design Departments ---");
        for (String line : lines) {
            DesignDepartment dept = DesignDepartment.fromCSV(line);
            int currentLoad = countDesignsInDepartment(dept.getId());
            double utilization = (dept.getMaxCapacity() > 0)
                ? (currentLoad * 100.0 / dept.getMaxCapacity()) : 0;
            System.out.println(dept + " | Load: " + currentLoad + "/" + dept.getMaxCapacity()
                             + " (" + String.format("%.0f", utilization) + "%)");
        }
    }

    // =========================================================================
    // 3. View Department Details
    // =========================================================================

    private void viewDepartmentDetails() {
        System.out.print("Enter Department ID: ");
        int deptId = readInt();
        if (deptId == -1) return;

        DesignDepartment dept = findDeptById(deptId);
        if (dept == null) {
            System.out.println("Error: Department not found.");
            return;
        }

        int currentLoad = countDesignsInDepartment(deptId);
        List<DesignReview> reviews = loadReviewsForDepartment(deptId);
        long approved = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.APPROVED).count();
        long rejected = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.REJECTED).count();
        long pending = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.PENDING
                                                 || r.getStatus() == DesignReview.Status.IN_REVIEW).count();
        long revNeeded = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.REVISION_NEEDED).count();
        double avgScore = reviews.stream().filter(r -> r.getOverallScore() > 0)
                                 .mapToDouble(DesignReview::getOverallScore).average().orElse(0);
        double budgetRemaining = dept.getBudget() - dept.getSpentBudget();
        double budgetUtilization = (dept.getBudget() > 0) ? (dept.getSpentBudget() / dept.getBudget() * 100) : 0;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║          DEPARTMENT DETAIL REPORT            ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  ID              : " + dept.getId());
        System.out.println("  Name            : " + dept.getName());
        System.out.println("  Head Designer   : " + dept.getHeadDesigner());
        System.out.println("  Season          : " + dept.getSeason());
        System.out.println("  Specialization  : " + dept.getSpecialization());
        System.out.println("  Status          : " + dept.getStatus());
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  BUDGET:");
        System.out.println("    Total          : $" + String.format("%.2f", dept.getBudget()));
        System.out.println("    Spent          : $" + String.format("%.2f", dept.getSpentBudget()));
        System.out.println("    Remaining      : $" + String.format("%.2f", budgetRemaining));
        System.out.println("    Utilization    : " + String.format("%.1f", budgetUtilization) + "%");
        if (budgetRemaining < dept.getBudget() * 0.1) {
            System.out.println("    *** WARNING: Budget nearly exhausted! ***");
        }
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  CAPACITY:");
        System.out.println("    Max Designs    : " + dept.getMaxCapacity());
        System.out.println("    Current Load   : " + currentLoad);
        System.out.println("    Available      : " + (dept.getMaxCapacity() - currentLoad));
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  REVIEW SUMMARY:");
        System.out.println("    Total Reviews  : " + reviews.size());
        System.out.println("    Approved       : " + approved);
        System.out.println("    Rejected       : " + rejected);
        System.out.println("    Revision Needed: " + revNeeded);
        System.out.println("    Pending/Active : " + pending);
        System.out.println("    Avg Score      : " + String.format("%.2f", avgScore) + "/10");

        // List garments currently under this department
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  ASSIGNED GARMENTS:");
        Set<Integer> garmentIds = reviews.stream().map(DesignReview::getGarmentDesignId)
                                         .collect(Collectors.toSet());
        if (garmentIds.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (int gid : garmentIds) {
                GarmentDesign g = GarmentDesignController.findById(gid);
                String gName = (g != null) ? g.getName() + " (" + g.getType() + ")" : "Unknown";
                DesignReview latestReview = getLatestReviewForGarment(gid, deptId);
                String reviewStatus = (latestReview != null) ? latestReview.getStatus().name() : "N/A";
                System.out.println("    - [" + gid + "] " + gName + " | Review: " + reviewStatus);
            }
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 4. Update Department Budget
    // =========================================================================

    private void updateDepartmentBudget() {
        System.out.print("Enter Department ID: ");
        int deptId = readInt();
        if (deptId == -1) return;

        DesignDepartment dept = findDeptById(deptId);
        if (dept == null) {
            System.out.println("Error: Department not found.");
            return;
        }

        System.out.println("Current budget: $" + String.format("%.2f", dept.getBudget())
                         + " | Spent: $" + String.format("%.2f", dept.getSpentBudget()));
        System.out.println("1. Increase budget");
        System.out.println("2. Decrease budget");
        System.out.println("3. Set new budget");
        System.out.print("Select: ");
        String choice = scanner.nextLine().trim();

        System.out.print("Amount ($): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid amount.");
            return;
        }
        if (amount <= 0) {
            System.out.println("Error: Amount must be positive.");
            return;
        }

        switch (choice) {
            case "1" -> {
                dept.setBudget(dept.getBudget() + amount);
                System.out.println("Budget increased. New budget: $" + String.format("%.2f", dept.getBudget()));
            }
            case "2" -> {
                if (dept.getBudget() - amount < dept.getSpentBudget()) {
                    System.out.println("Error: Cannot reduce budget below already spent amount ($"
                                     + String.format("%.2f", dept.getSpentBudget()) + ").");
                    return;
                }
                dept.setBudget(dept.getBudget() - amount);
                System.out.println("Budget decreased. New budget: $" + String.format("%.2f", dept.getBudget()));
            }
            case "3" -> {
                if (amount < dept.getSpentBudget()) {
                    System.out.println("Error: New budget cannot be less than already spent amount ($"
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
        updateDept(dept);
    }

    // =========================================================================
    // 5. Change Department Status
    // =========================================================================

    private void changeDepartmentStatus() {
        System.out.print("Enter Department ID: ");
        int deptId = readInt();
        if (deptId == -1) return;

        DesignDepartment dept = findDeptById(deptId);
        if (dept == null) {
            System.out.println("Error: Department not found.");
            return;
        }

        System.out.println("Current status: " + dept.getStatus());
        System.out.println("1. ACTIVE");
        System.out.println("2. PLANNING");
        System.out.println("3. ON_HOLD");
        System.out.println("4. CLOSED");
        System.out.print("New status: ");
        String choice = scanner.nextLine().trim();
        DesignDepartment.Status newStatus;
        switch (choice) {
            case "1" -> newStatus = DesignDepartment.Status.ACTIVE;
            case "2" -> newStatus = DesignDepartment.Status.PLANNING;
            case "3" -> newStatus = DesignDepartment.Status.ON_HOLD;
            case "4" -> {
                // Check for pending reviews before closing
                long pendingReviews = loadReviewsForDepartment(deptId).stream()
                    .filter(r -> r.getStatus() == DesignReview.Status.PENDING
                              || r.getStatus() == DesignReview.Status.IN_REVIEW).count();
                if (pendingReviews > 0) {
                    System.out.println("Warning: " + pendingReviews + " review(s) still pending in this department.");
                    System.out.print("Close anyway? (yes/no): ");
                    if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                        System.out.println("Status change cancelled.");
                        return;
                    }
                }
                newStatus = DesignDepartment.Status.CLOSED;
            }
            default -> {
                System.out.println("Invalid option.");
                return;
            }
        }

        dept.setStatus(newStatus);
        updateDept(dept);
        System.out.println("Department status updated to: " + newStatus);
    }

    // =========================================================================
    // 6. Submit Design for Review
    // =========================================================================

    private void submitDesignForReview() {
        System.out.println("\n--- Submit Design for Review ---");

        // Check preconditions
        if (!FileManager.hasRecords(DEPT_FILE)) {
            System.out.println("Error: No departments exist. Create a department first.");
            return;
        }
        if (!FileManager.hasRecords(GarmentDesignController.FILE)) {
            System.out.println("Error: No garment designs exist.");
            return;
        }

        // Show available garments
        System.out.println("\n--- Available Garment Designs ---");
        for (String line : FileManager.readLines(GarmentDesignController.FILE)) {
            GarmentDesign g = GarmentDesign.fromCSV(line);
            org.example.model.Collection col = CollectionController.findById(g.getCollectionId());
            String colName = (col != null) ? col.getName() : "Unknown";
            System.out.println("  " + g + " | Collection: " + colName);
        }

        System.out.print("\nEnter Garment Design ID: ");
        int garmentId = readInt();
        if (garmentId == -1) return;
        GarmentDesign garment = GarmentDesignController.findById(garmentId);
        if (garment == null) {
            System.out.println("Error: Garment design not found.");
            return;
        }

        // Show available departments
        System.out.println("\n--- Active Departments ---");
        List<DesignDepartment> activeDepts = new ArrayList<>();
        for (String line : FileManager.readLines(DEPT_FILE)) {
            DesignDepartment d = DesignDepartment.fromCSV(line);
            if (d.getStatus() == DesignDepartment.Status.ACTIVE) {
                int load = countDesignsInDepartment(d.getId());
                activeDepts.add(d);
                System.out.println("  " + d + " | Current Load: " + load + "/" + d.getMaxCapacity());
            }
        }
        if (activeDepts.isEmpty()) {
            System.out.println("Error: No active departments available.");
            return;
        }

        System.out.print("\nEnter Department ID: ");
        int deptId = readInt();
        if (deptId == -1) return;
        DesignDepartment dept = findDeptById(deptId);
        if (dept == null || dept.getStatus() != DesignDepartment.Status.ACTIVE) {
            System.out.println("Error: Department not found or not active.");
            return;
        }

        // Capacity check
        int currentLoad = countDesignsInDepartment(deptId);
        if (currentLoad >= dept.getMaxCapacity()) {
            System.out.println("Error: Department at full capacity (" + currentLoad
                             + "/" + dept.getMaxCapacity() + "). Cannot accept more designs.");
            return;
        }

        // Check if already submitted and pending
        DesignReview existingPending = getLatestReviewForGarment(garmentId, deptId);
        if (existingPending != null && (existingPending.getStatus() == DesignReview.Status.PENDING
                                     || existingPending.getStatus() == DesignReview.Status.IN_REVIEW)) {
            System.out.println("Error: This design already has a pending/active review in this department (Review #"
                             + existingPending.getId() + ").");
            return;
        }

        // Budget check for review cost
        double budgetRemaining = dept.getBudget() - dept.getSpentBudget();
        if (budgetRemaining < REVIEW_COST_PER_DESIGN) {
            System.out.println("Error: Insufficient department budget for review. Required: $"
                             + String.format("%.2f", REVIEW_COST_PER_DESIGN)
                             + " | Available: $" + String.format("%.2f", budgetRemaining));
            return;
        }

        // Reviewer name
        System.out.print("Assigned reviewer name: ");
        String reviewer = scanner.nextLine().trim();
        if (reviewer.isEmpty()) {
            System.out.println("Error: Reviewer name is required.");
            return;
        }

        // Create review with PENDING status, scores all 0 until conducted
        int reviewId = FileManager.nextId(REVIEW_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        DesignReview review = new DesignReview(reviewId, garmentId, deptId, reviewer,
                                               0, 0, 0, 0, 0, 0,
                                               DesignReview.Status.PENDING, "Awaiting review", today);
        FileManager.appendLine(REVIEW_FILE, review.toCSV());

        // Deduct review cost from budget
        dept.setSpentBudget(dept.getSpentBudget() + REVIEW_COST_PER_DESIGN);
        updateDept(dept);

        System.out.println("\nDesign submitted for review successfully!");
        System.out.println("Review ID: " + reviewId + " | Garment: " + garment.getName()
                         + " | Department: " + dept.getName() + " | Reviewer: " + reviewer);
        System.out.println("Review cost of $" + String.format("%.2f", REVIEW_COST_PER_DESIGN)
                         + " charged to department budget.");
    }

    // =========================================================================
    // 7. Conduct Design Review (Score the design)
    // =========================================================================

    private void conductDesignReview() {
        System.out.println("\n--- Conduct Design Review ---");

        // Show pending reviews
        List<DesignReview> pendingReviews = loadAllReviews().stream()
            .filter(r -> r.getStatus() == DesignReview.Status.PENDING
                      || r.getStatus() == DesignReview.Status.IN_REVIEW)
            .collect(Collectors.toList());

        if (pendingReviews.isEmpty()) {
            System.out.println("No pending reviews to conduct.");
            return;
        }

        System.out.println("\n--- Pending Reviews ---");
        for (DesignReview r : pendingReviews) {
            GarmentDesign g = GarmentDesignController.findById(r.getGarmentDesignId());
            String gName = (g != null) ? g.getName() : "Unknown";
            System.out.println("  Review #" + r.getId() + " | Garment: " + gName
                             + " [ID:" + r.getGarmentDesignId() + "]"
                             + " | Reviewer: " + r.getReviewerName()
                             + " | Status: " + r.getStatus());
        }

        System.out.print("\nEnter Review ID to conduct: ");
        int reviewId = readInt();
        if (reviewId == -1) return;

        DesignReview review = findReviewById(reviewId);
        if (review == null) {
            System.out.println("Error: Review not found.");
            return;
        }
        if (review.getStatus() != DesignReview.Status.PENDING
                && review.getStatus() != DesignReview.Status.IN_REVIEW) {
            System.out.println("Error: Review is not in a reviewable state (current: " + review.getStatus() + ").");
            return;
        }

        GarmentDesign garment = GarmentDesignController.findById(review.getGarmentDesignId());
        if (garment != null) {
            System.out.println("\nReviewing: " + garment);
            ProductSpecification spec = findSpecForGarment(garment.getId());
            if (spec != null) {
                System.out.println("Spec: " + spec);
            }
        }

        System.out.println("\nScore each criterion (0.0 - 10.0):");
        System.out.println("  Weights: Creativity=" + (int)(WEIGHT_CREATIVITY*100) + "%"
                         + " Feasibility=" + (int)(WEIGHT_FEASIBILITY*100) + "%"
                         + " MarketFit=" + (int)(WEIGHT_MARKET_FIT*100) + "%"
                         + " CostEfficiency=" + (int)(WEIGHT_COST_EFFICIENCY*100) + "%"
                         + " BrandAlignment=" + (int)(WEIGHT_BRAND_ALIGNMENT*100) + "%");

        double creativity = readScore("Creativity (originality, innovation)");
        if (creativity < 0) return;
        double feasibility = readScore("Feasibility (can it be manufactured?)");
        if (feasibility < 0) return;
        double marketFit = readScore("Market Fit (target audience appeal)");
        if (marketFit < 0) return;
        double costEfficiency = readScore("Cost Efficiency (materials, production cost)");
        if (costEfficiency < 0) return;
        double brandAlignment = readScore("Brand Alignment (fits brand identity)");
        if (brandAlignment < 0) return;

        // Calculate weighted overall score
        double overall = (creativity * WEIGHT_CREATIVITY)
                       + (feasibility * WEIGHT_FEASIBILITY)
                       + (marketFit * WEIGHT_MARKET_FIT)
                       + (costEfficiency * WEIGHT_COST_EFFICIENCY)
                       + (brandAlignment * WEIGHT_BRAND_ALIGNMENT);

        // Determine status based on score
        DesignReview.Status newStatus;
        String autoFeedback;
        if (overall >= APPROVAL_THRESHOLD) {
            newStatus = DesignReview.Status.APPROVED;
            autoFeedback = "APPROVED - Meets quality standards";
        } else if (overall >= REVISION_THRESHOLD) {
            newStatus = DesignReview.Status.REVISION_NEEDED;
            autoFeedback = "REVISION NEEDED - Below approval threshold";
        } else {
            newStatus = DesignReview.Status.REJECTED;
            autoFeedback = "REJECTED - Does not meet minimum standards";
        }

        // Optional reviewer feedback
        System.out.print("\nAdditional feedback (optional): ");
        String customFeedback = scanner.nextLine().trim();
        String finalFeedback = customFeedback.isEmpty() ? autoFeedback : autoFeedback + " | " + customFeedback;

        // Display score summary
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║            REVIEW SCORE SUMMARY              ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("  Creativity        : " + String.format("%.1f", creativity) + "/10  (x" + (int)(WEIGHT_CREATIVITY*100) + "%)");
        System.out.println("  Feasibility       : " + String.format("%.1f", feasibility) + "/10  (x" + (int)(WEIGHT_FEASIBILITY*100) + "%)");
        System.out.println("  Market Fit        : " + String.format("%.1f", marketFit) + "/10  (x" + (int)(WEIGHT_MARKET_FIT*100) + "%)");
        System.out.println("  Cost Efficiency   : " + String.format("%.1f", costEfficiency) + "/10  (x" + (int)(WEIGHT_COST_EFFICIENCY*100) + "%)");
        System.out.println("  Brand Alignment   : " + String.format("%.1f", brandAlignment) + "/10  (x" + (int)(WEIGHT_BRAND_ALIGNMENT*100) + "%)");
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  WEIGHTED OVERALL  : " + String.format("%.2f", overall) + "/10");
        System.out.println("  DECISION          : " + newStatus);
        System.out.println("  (Approval >= " + APPROVAL_THRESHOLD + " | Revision >= " + REVISION_THRESHOLD + " | Reject < " + REVISION_THRESHOLD + ")");
        System.out.println("╚══════════════════════════════════════════════╝");

        System.out.print("\nConfirm review scores? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Review cancelled.");
            return;
        }

        // Update review in file
        List<String> lines = FileManager.readLines(REVIEW_FILE);
        for (int i = 0; i < lines.size(); i++) {
            DesignReview r = DesignReview.fromCSV(lines.get(i));
            if (r.getId() == reviewId) {
                DesignReview updated = new DesignReview(r.getId(), r.getGarmentDesignId(), r.getDepartmentId(),
                    r.getReviewerName(), creativity, feasibility, marketFit, costEfficiency, brandAlignment,
                    overall, newStatus, finalFeedback, LocalDate.now().format(DATE_FMT));
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(REVIEW_FILE, lines);

        System.out.println("Review completed. Status: " + newStatus);
        if (newStatus == DesignReview.Status.REVISION_NEEDED) {
            System.out.println("The designer should revise the design and resubmit (use option 9 to record revision).");
        }
    }

    // =========================================================================
    // 8. View Review Pipeline
    // =========================================================================

    private void viewReviewPipeline() {
        List<DesignReview> all = loadAllReviews();
        if (all.isEmpty()) {
            System.out.println("No reviews on file.");
            return;
        }

        Map<DesignReview.Status, List<DesignReview>> grouped = all.stream()
            .collect(Collectors.groupingBy(DesignReview::getStatus));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║          DESIGN REVIEW PIPELINE              ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        for (DesignReview.Status status : DesignReview.Status.values()) {
            List<DesignReview> group = grouped.getOrDefault(status, Collections.emptyList());
            System.out.println("\n  ▸ " + status + " (" + group.size() + ")");
            if (group.isEmpty()) {
                System.out.println("    (none)");
            } else {
                for (DesignReview r : group) {
                    GarmentDesign g = GarmentDesignController.findById(r.getGarmentDesignId());
                    String gName = (g != null) ? g.getName() : "Unknown";
                    System.out.println("    Review #" + r.getId() + " | " + gName
                                     + " | Score: " + String.format("%.1f", r.getOverallScore())
                                     + " | Reviewer: " + r.getReviewerName()
                                     + " | Date: " + r.getReviewDate());
                }
            }
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 9. Record Design Revision
    // =========================================================================

    private void recordDesignRevision() {
        System.out.println("\n--- Record Design Revision ---");

        // Show designs that need revision
        List<DesignReview> revisionNeeded = loadAllReviews().stream()
            .filter(r -> r.getStatus() == DesignReview.Status.REVISION_NEEDED)
            .collect(Collectors.toList());

        if (revisionNeeded.isEmpty()) {
            System.out.println("No designs currently need revision.");
            return;
        }

        System.out.println("Designs needing revision:");
        for (DesignReview r : revisionNeeded) {
            GarmentDesign g = GarmentDesignController.findById(r.getGarmentDesignId());
            String gName = (g != null) ? g.getName() : "Unknown";
            System.out.println("  Review #" + r.getId() + " | Garment: " + gName
                             + " [ID:" + r.getGarmentDesignId() + "]"
                             + " | Score: " + String.format("%.1f", r.getOverallScore())
                             + " | Feedback: " + r.getFeedback());
        }

        System.out.print("\nEnter Garment Design ID to revise: ");
        int garmentId = readInt();
        if (garmentId == -1) return;
        GarmentDesign garment = GarmentDesignController.findById(garmentId);
        if (garment == null) {
            System.out.println("Error: Garment not found.");
            return;
        }

        // Find the review that triggered this
        System.out.print("Enter Review ID that triggered this revision: ");
        int reviewId = readInt();
        if (reviewId == -1) return;
        DesignReview review = findReviewById(reviewId);
        if (review == null || review.getGarmentDesignId() != garmentId) {
            System.out.println("Error: Review not found or does not match this garment.");
            return;
        }

        // Get next revision number
        int revNum = getNextRevisionNumber(garmentId);

        System.out.print("Revised by (designer name): ");
        String revisedBy = scanner.nextLine().trim();
        if (revisedBy.isEmpty()) {
            System.out.println("Error: Designer name required.");
            return;
        }

        System.out.println("Describe the changes made:");
        System.out.print("  Change description: ");
        String changes = scanner.nextLine().trim();
        if (changes.isEmpty()) {
            System.out.println("Error: Change description required.");
            return;
        }

        System.out.print("Budget impact of this revision ($, 0 if none): ");
        double budgetImpact;
        try {
            budgetImpact = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid amount.");
            return;
        }

        // Record revision
        int revId = FileManager.nextId(REVISION_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        DesignRevision revision = new DesignRevision(revId, garmentId, revNum, changes,
                                                      revisedBy, today, reviewId, budgetImpact);
        FileManager.appendLine(REVISION_FILE, revision.toCSV());

        // Update department budget if there's impact
        if (budgetImpact > 0) {
            DesignDepartment dept = findDeptById(review.getDepartmentId());
            if (dept != null) {
                dept.setSpentBudget(dept.getSpentBudget() + budgetImpact);
                updateDept(dept);
                System.out.println("$" + String.format("%.2f", budgetImpact)
                                 + " charged to department '" + dept.getName() + "'.");
            }
        }

        System.out.println("\nRevision recorded: " + revision);
        System.out.println("The design can now be resubmitted for review (option 6).");
    }

    // =========================================================================
    // 10. View Revision History
    // =========================================================================

    private void viewRevisionHistory() {
        System.out.print("Enter Garment Design ID (or 0 for all): ");
        int garmentId = readInt();
        if (garmentId == -1) return;

        List<String> lines = FileManager.readLines(REVISION_FILE);
        if (lines.isEmpty()) {
            System.out.println("No revision history on file.");
            return;
        }

        List<DesignRevision> revisions = new ArrayList<>();
        for (String line : lines) {
            DesignRevision rev = DesignRevision.fromCSV(line);
            if (garmentId == 0 || rev.getGarmentDesignId() == garmentId) {
                revisions.add(rev);
            }
        }

        if (revisions.isEmpty()) {
            System.out.println("No revisions found for garment ID " + garmentId + ".");
            return;
        }

        System.out.println("\n--- Revision History ---");
        for (DesignRevision rev : revisions) {
            GarmentDesign g = GarmentDesignController.findById(rev.getGarmentDesignId());
            String gName = (g != null) ? g.getName() : "Unknown";
            System.out.println("  " + rev + " | Garment: " + gName);
        }
        System.out.println("Total revisions: " + revisions.size());
        double totalImpact = revisions.stream().mapToDouble(DesignRevision::getBudgetImpact).sum();
        System.out.println("Total budget impact: $" + String.format("%.2f", totalImpact));
    }

    // =========================================================================
    // 11. Transfer Design Between Departments
    // =========================================================================

    private void transferDesign() {
        System.out.println("\n--- Transfer Design Between Departments ---");

        System.out.print("Enter Garment Design ID: ");
        int garmentId = readInt();
        if (garmentId == -1) return;
        GarmentDesign garment = GarmentDesignController.findById(garmentId);
        if (garment == null) {
            System.out.println("Error: Garment not found.");
            return;
        }

        System.out.print("Enter source Department ID: ");
        int srcDeptId = readInt();
        if (srcDeptId == -1) return;
        DesignDepartment srcDept = findDeptById(srcDeptId);
        if (srcDept == null) {
            System.out.println("Error: Source department not found.");
            return;
        }

        // Check garment is in source department
        DesignReview latestReview = getLatestReviewForGarment(garmentId, srcDeptId);
        if (latestReview == null) {
            System.out.println("Error: This garment has no reviews in department '" + srcDept.getName() + "'.");
            return;
        }

        System.out.print("Enter destination Department ID: ");
        int destDeptId = readInt();
        if (destDeptId == -1) return;
        if (destDeptId == srcDeptId) {
            System.out.println("Error: Source and destination departments are the same.");
            return;
        }
        DesignDepartment destDept = findDeptById(destDeptId);
        if (destDept == null || destDept.getStatus() != DesignDepartment.Status.ACTIVE) {
            System.out.println("Error: Destination department not found or not active.");
            return;
        }

        // Capacity check on destination
        int destLoad = countDesignsInDepartment(destDeptId);
        if (destLoad >= destDept.getMaxCapacity()) {
            System.out.println("Error: Destination department at capacity.");
            return;
        }

        // Budget check on destination
        double destRemaining = destDept.getBudget() - destDept.getSpentBudget();
        if (destRemaining < REVIEW_COST_PER_DESIGN) {
            System.out.println("Error: Destination department has insufficient budget for new review.");
            return;
        }

        System.out.print("Assigned reviewer in new department: ");
        String reviewer = scanner.nextLine().trim();
        if (reviewer.isEmpty()) {
            System.out.println("Error: Reviewer name required.");
            return;
        }

        System.out.println("\nTransfer summary:");
        System.out.println("  Garment: " + garment.getName() + " [ID:" + garmentId + "]");
        System.out.println("  From: " + srcDept.getName() + " → To: " + destDept.getName());
        System.out.println("  New reviewer: " + reviewer);
        System.out.print("Confirm transfer? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Transfer cancelled.");
            return;
        }

        // Create new review in destination department
        int reviewId = FileManager.nextId(REVIEW_FILE);
        String today = LocalDate.now().format(DATE_FMT);
        DesignReview newReview = new DesignReview(reviewId, garmentId, destDeptId, reviewer,
                                                   0, 0, 0, 0, 0, 0,
                                                   DesignReview.Status.PENDING,
                                                   "Transferred from " + srcDept.getName(), today);
        FileManager.appendLine(REVIEW_FILE, newReview.toCSV());

        // Charge destination department
        destDept.setSpentBudget(destDept.getSpentBudget() + REVIEW_COST_PER_DESIGN);
        updateDept(destDept);

        System.out.println("Design transferred successfully. New Review ID: " + reviewId);
    }

    // =========================================================================
    // 12. Department Workload Analysis
    // =========================================================================

    private void departmentWorkloadAnalysis() {
        List<String> deptLines = FileManager.readLines(DEPT_FILE);
        if (deptLines.isEmpty()) {
            System.out.println("No departments on file.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║          DEPARTMENT WORKLOAD ANALYSIS                ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        for (String line : deptLines) {
            DesignDepartment dept = DesignDepartment.fromCSV(line);
            int load = countDesignsInDepartment(dept.getId());
            double utilization = (dept.getMaxCapacity() > 0) ? (load * 100.0 / dept.getMaxCapacity()) : 0;

            List<DesignReview> reviews = loadReviewsForDepartment(dept.getId());
            long pending = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.PENDING).count();
            long inReview = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.IN_REVIEW).count();
            long revNeeded = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.REVISION_NEEDED).count();
            long approved = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.APPROVED).count();
            long rejected = reviews.stream().filter(r -> r.getStatus() == DesignReview.Status.REJECTED).count();

            double budgetUsed = (dept.getBudget() > 0) ? (dept.getSpentBudget() / dept.getBudget() * 100) : 0;

            String loadBar = generateBar(utilization);
            String budgetBar = generateBar(budgetUsed);

            System.out.println("\n  " + dept.getName() + " [" + dept.getStatus() + "]");
            System.out.println("    Head: " + dept.getHeadDesigner() + " | Spec: " + dept.getSpecialization());
            System.out.println("    Capacity : " + loadBar + " " + load + "/" + dept.getMaxCapacity()
                             + " (" + String.format("%.0f", utilization) + "%)");
            System.out.println("    Budget   : " + budgetBar + " $" + String.format("%.0f", dept.getSpentBudget())
                             + "/$" + String.format("%.0f", dept.getBudget())
                             + " (" + String.format("%.0f", budgetUsed) + "%)");
            System.out.println("    Pipeline : Pending=" + pending + " InReview=" + inReview
                             + " RevNeeded=" + revNeeded + " Approved=" + approved
                             + " Rejected=" + rejected);
            if (utilization > 90) System.out.println("    *** OVERLOADED ***");
            if (budgetUsed > 90) System.out.println("    *** BUDGET CRITICAL ***");
        }
        System.out.println("\n╚══════════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 13. Department Performance Dashboard
    // =========================================================================

    private void departmentPerformanceDashboard() {
        List<DesignReview> allReviews = loadAllReviews();
        List<String> deptLines = FileManager.readLines(DEPT_FILE);

        if (deptLines.isEmpty() || allReviews.isEmpty()) {
            System.out.println("Insufficient data for performance dashboard.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║         DEPARTMENT PERFORMANCE DASHBOARD                 ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        // Global metrics
        double globalAvgScore = allReviews.stream().filter(r -> r.getOverallScore() > 0)
            .mapToDouble(DesignReview::getOverallScore).average().orElse(0);
        long totalApproved = allReviews.stream().filter(r -> r.getStatus() == DesignReview.Status.APPROVED).count();
        long totalRejected = allReviews.stream().filter(r -> r.getStatus() == DesignReview.Status.REJECTED).count();
        long totalCompleted = totalApproved + totalRejected;
        double approvalRate = (totalCompleted > 0) ? (totalApproved * 100.0 / totalCompleted) : 0;

        System.out.println("  GLOBAL METRICS:");
        System.out.println("    Total Reviews        : " + allReviews.size());
        System.out.println("    Global Avg Score     : " + String.format("%.2f", globalAvgScore) + "/10");
        System.out.println("    Approval Rate        : " + String.format("%.1f", approvalRate) + "%");
        System.out.println("    Total Approved       : " + totalApproved);
        System.out.println("    Total Rejected       : " + totalRejected);

        // Per-criterion global averages
        double avgCreativity = allReviews.stream().filter(r -> r.getCreativityScore() > 0)
            .mapToDouble(DesignReview::getCreativityScore).average().orElse(0);
        double avgFeasibility = allReviews.stream().filter(r -> r.getFeasibilityScore() > 0)
            .mapToDouble(DesignReview::getFeasibilityScore).average().orElse(0);
        double avgMarketFit = allReviews.stream().filter(r -> r.getMarketFitScore() > 0)
            .mapToDouble(DesignReview::getMarketFitScore).average().orElse(0);
        double avgCostEff = allReviews.stream().filter(r -> r.getCostEfficiencyScore() > 0)
            .mapToDouble(DesignReview::getCostEfficiencyScore).average().orElse(0);
        double avgBrand = allReviews.stream().filter(r -> r.getBrandAlignmentScore() > 0)
            .mapToDouble(DesignReview::getBrandAlignmentScore).average().orElse(0);

        System.out.println("  ──────────────────────────────────────");
        System.out.println("  CRITERIA AVERAGES:");
        System.out.println("    Creativity           : " + String.format("%.2f", avgCreativity));
        System.out.println("    Feasibility          : " + String.format("%.2f", avgFeasibility));
        System.out.println("    Market Fit           : " + String.format("%.2f", avgMarketFit));
        System.out.println("    Cost Efficiency      : " + String.format("%.2f", avgCostEff));
        System.out.println("    Brand Alignment      : " + String.format("%.2f", avgBrand));

        // Identify weakest criterion
        Map<String, Double> criteria = new LinkedHashMap<>();
        criteria.put("Creativity", avgCreativity);
        criteria.put("Feasibility", avgFeasibility);
        criteria.put("Market Fit", avgMarketFit);
        criteria.put("Cost Efficiency", avgCostEff);
        criteria.put("Brand Alignment", avgBrand);
        String weakest = criteria.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");
        String strongest = criteria.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");
        System.out.println("    Strongest            : " + strongest);
        System.out.println("    Needs Improvement    : " + weakest);

        // Per-department breakdown
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  PER-DEPARTMENT BREAKDOWN:");
        for (String dLine : deptLines) {
            DesignDepartment dept = DesignDepartment.fromCSV(dLine);
            List<DesignReview> deptReviews = allReviews.stream()
                .filter(r -> r.getDepartmentId() == dept.getId())
                .collect(Collectors.toList());
            if (deptReviews.isEmpty()) continue;

            double deptAvg = deptReviews.stream().filter(r -> r.getOverallScore() > 0)
                .mapToDouble(DesignReview::getOverallScore).average().orElse(0);
            long dApproved = deptReviews.stream().filter(r -> r.getStatus() == DesignReview.Status.APPROVED).count();
            long dTotal = deptReviews.stream()
                .filter(r -> r.getStatus() == DesignReview.Status.APPROVED
                          || r.getStatus() == DesignReview.Status.REJECTED).count();
            double dRate = (dTotal > 0) ? (dApproved * 100.0 / dTotal) : 0;

            // Count revisions for this department's garments
            long revisionCount = 0;
            for (String rl : FileManager.readLines(REVISION_FILE)) {
                DesignRevision rev = DesignRevision.fromCSV(rl);
                if (deptReviews.stream().anyMatch(r -> r.getGarmentDesignId() == rev.getGarmentDesignId())) {
                    revisionCount++;
                }
            }

            System.out.println("\n    " + dept.getName() + " (" + dept.getSpecialization() + ")");
            System.out.println("      Reviews: " + deptReviews.size() + " | Avg Score: "
                             + String.format("%.2f", deptAvg) + " | Approval Rate: "
                             + String.format("%.0f", dRate) + "% | Revisions: " + revisionCount);
        }

        // Total budget across all departments
        double totalBudget = 0, totalSpent = 0;
        for (String dLine : deptLines) {
            DesignDepartment dept = DesignDepartment.fromCSV(dLine);
            totalBudget += dept.getBudget();
            totalSpent += dept.getSpentBudget();
        }
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  TOTAL BUDGET OVERVIEW:");
        System.out.println("    Total Allocated      : $" + String.format("%.2f", totalBudget));
        System.out.println("    Total Spent          : $" + String.format("%.2f", totalSpent));
        System.out.println("    Total Remaining      : $" + String.format("%.2f", totalBudget - totalSpent));
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 14. Review Audit Trail
    // =========================================================================

    private void reviewAuditTrail() {
        System.out.print("Enter Garment Design ID (or 0 for all): ");
        int garmentId = readInt();
        if (garmentId == -1) return;

        List<DesignReview> reviews = loadAllReviews();
        List<String> revisionLines = FileManager.readLines(REVISION_FILE);

        if (garmentId > 0) {
            reviews = reviews.stream().filter(r -> r.getGarmentDesignId() == garmentId)
                             .collect(Collectors.toList());
        }

        if (reviews.isEmpty()) {
            System.out.println("No audit trail found.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║            DESIGN REVIEW AUDIT TRAIL             ║");
        System.out.println("╠══════════════════════════════════════════════════╣");

        // Group by garment
        Map<Integer, List<DesignReview>> byGarment = reviews.stream()
            .collect(Collectors.groupingBy(DesignReview::getGarmentDesignId));

        for (Map.Entry<Integer, List<DesignReview>> entry : byGarment.entrySet()) {
            GarmentDesign g = GarmentDesignController.findById(entry.getKey());
            String gName = (g != null) ? g.getName() : "Unknown";

            System.out.println("\n  Garment: " + gName + " [ID:" + entry.getKey() + "]");
            System.out.println("  ────────────────────────────────────────");

            // Sort reviews by date
            List<DesignReview> gReviews = entry.getValue();
            gReviews.sort(Comparator.comparing(DesignReview::getReviewDate));

            for (DesignReview r : gReviews) {
                DesignDepartment dept = findDeptById(r.getDepartmentId());
                String deptName = (dept != null) ? dept.getName() : "Unknown";
                System.out.println("    [" + r.getReviewDate() + "] Review #" + r.getId()
                                 + " | Dept: " + deptName + " | Reviewer: " + r.getReviewerName()
                                 + " | Score: " + String.format("%.1f", r.getOverallScore())
                                 + " | " + r.getStatus());
                if (!r.getFeedback().equals("Awaiting review")) {
                    System.out.println("      Feedback: " + r.getFeedback());
                }

                // Show revisions triggered by this review
                for (String rl : revisionLines) {
                    DesignRevision rev = DesignRevision.fromCSV(rl);
                    if (rev.getPreviousReviewId() == r.getId()) {
                        System.out.println("      → Revision #" + rev.getRevisionNumber()
                                         + " by " + rev.getRevisedBy()
                                         + " on " + rev.getRevisionDate()
                                         + " | Changes: " + rev.getChangeDescription()
                                         + " | Cost: $" + String.format("%.2f", rev.getBudgetImpact()));
                    }
                }
            }
        }
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // 15. Assign Employee to Department
    // =========================================================================

    private void assignEmployeeToDepartment() {
        System.out.println("\n--- Assign Employee to Department ---");

        if (!FileManager.hasRecords(DEPT_FILE)) {
            System.out.println("Error: No departments exist. Create a department first.");
            return;
        }

        System.out.println("\nAvailable Departments:");
        for (String line : FileManager.readLines(DEPT_FILE)) {
            DesignDepartment dept = DesignDepartment.fromCSV(line);
            System.out.println("  [" + dept.getId() + "] " + dept.getName() + " | Status: " + dept.getStatus()
                             + " | Head: " + dept.getHeadDesigner());
        }

        System.out.print("Enter Department ID: ");
        int deptId = readInt();
        if (deptId == -1) return;

        DesignDepartment dept = findDeptById(deptId);
        if (dept == null) {
            System.out.println("Error: Department not found.");
            return;
        }
        if (dept.getStatus() == DesignDepartment.Status.CLOSED) {
            System.out.println("Error: Cannot assign employees to a CLOSED department.");
            return;
        }

        System.out.print("Employee full name: ");
        String employeeName = scanner.nextLine().trim();
        if (employeeName.isEmpty()) {
            System.out.println("Error: Employee name is required.");
            return;
        }

        System.out.print("Role (e.g. Designer, Pattern Maker, Illustrator): ");
        String role = scanner.nextLine().trim();
        if (role.isEmpty()) {
            System.out.println("Error: Role is required.");
            return;
        }

        List<String> lines = FileManager.readLines(ASSIGN_FILE);
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length < 6) continue;
            int assignedDeptId;
            try {
                assignedDeptId = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            String assignedEmployee = parts[2].trim();
            String assignedStatus = parts[5].trim();
            if (assignedEmployee.equalsIgnoreCase(employeeName)
                    && assignedDeptId == deptId
                    && assignedStatus.equalsIgnoreCase("active")) {
                System.out.println("Error: Employee is already actively assigned to this department.");
                return;
            }
        }

        int id = FileManager.nextId(ASSIGN_FILE);
        String assignedDate = LocalDate.now().format(DATE_FMT);
        String status = "active";
        String csv = id + "," + deptId + "," + employeeName + "," + role + "," + assignedDate + "," + status;
        FileManager.appendLine(ASSIGN_FILE, csv);

        System.out.println("Employee assigned successfully.");
        System.out.println("Assignment ID: " + id + " | Employee: " + employeeName
                         + " | Department: " + dept.getName() + " | Role: " + role);
    }

    // =========================================================================
    // 16. View Department Employee Assignments
    // =========================================================================

    private void viewDepartmentEmployeeAssignments() {
        List<String> lines = FileManager.readLines(ASSIGN_FILE);
        if (lines.isEmpty()) {
            System.out.println("No employee assignments on file.");
            return;
        }

        System.out.print("Enter Department ID (or 0 for all): ");
        int deptId = readInt();
        if (deptId == -1) return;

        if (deptId != 0 && findDeptById(deptId) == null) {
            System.out.println("Error: Department not found.");
            return;
        }

        int count = 0;
        System.out.println("\n--- Department Employee Assignments ---");
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length < 6) continue;

            int rowDeptId;
            try {
                rowDeptId = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                continue;
            }

            if (deptId != 0 && rowDeptId != deptId) {
                continue;
            }

            DesignDepartment rowDept = findDeptById(rowDeptId);
            String deptName = (rowDept != null) ? rowDept.getName() : "Unknown";

            System.out.println("[" + parts[0].trim() + "] " + parts[2].trim()
                             + " | Role: " + parts[3].trim()
                             + " | Department: " + deptName
                             + " | Assigned: " + parts[4].trim()
                             + " | Status: " + parts[5].trim());
            count++;
        }

        if (count == 0) {
            System.out.println("No assignments match the selected department.");
        } else {
            System.out.println("Total assignments shown: " + count);
        }
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private DesignDepartment findDeptById(int id) {
        for (String line : FileManager.readLines(DEPT_FILE)) {
            DesignDepartment d = DesignDepartment.fromCSV(line);
            if (d.getId() == id) return d;
        }
        return null;
    }

    private void updateDept(DesignDepartment updated) {
        List<String> lines = FileManager.readLines(DEPT_FILE);
        for (int i = 0; i < lines.size(); i++) {
            DesignDepartment d = DesignDepartment.fromCSV(lines.get(i));
            if (d.getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(DEPT_FILE, lines);
    }

    private DesignReview findReviewById(int id) {
        for (String line : FileManager.readLines(REVIEW_FILE)) {
            DesignReview r = DesignReview.fromCSV(line);
            if (r.getId() == id) return r;
        }
        return null;
    }

    private List<DesignReview> loadAllReviews() {
        List<DesignReview> reviews = new ArrayList<>();
        for (String line : FileManager.readLines(REVIEW_FILE)) {
            reviews.add(DesignReview.fromCSV(line));
        }
        return reviews;
    }

    private List<DesignReview> loadReviewsForDepartment(int deptId) {
        return loadAllReviews().stream()
            .filter(r -> r.getDepartmentId() == deptId)
            .collect(Collectors.toList());
    }

    private int countDesignsInDepartment(int deptId) {
        Set<Integer> garmentIds = loadReviewsForDepartment(deptId).stream()
            .filter(r -> r.getStatus() != DesignReview.Status.REJECTED)
            .map(DesignReview::getGarmentDesignId)
            .collect(Collectors.toSet());
        return garmentIds.size();
    }

    private DesignReview getLatestReviewForGarment(int garmentId, int deptId) {
        DesignReview latest = null;
        for (DesignReview r : loadReviewsForDepartment(deptId)) {
            if (r.getGarmentDesignId() == garmentId) {
                if (latest == null || r.getId() > latest.getId()) {
                    latest = r;
                }
            }
        }
        return latest;
    }

    private ProductSpecification findSpecForGarment(int garmentId) {
        for (String line : FileManager.readLines(ProductSpecificationController.FILE)) {
            ProductSpecification s = ProductSpecification.fromCSV(line);
            if (s.getGarmentDesignId() == garmentId) return s;
        }
        return null;
    }

    private int getNextRevisionNumber(int garmentId) {
        int max = 0;
        for (String line : FileManager.readLines(REVISION_FILE)) {
            DesignRevision rev = DesignRevision.fromCSV(line);
            if (rev.getGarmentDesignId() == garmentId && rev.getRevisionNumber() > max) {
                max = rev.getRevisionNumber();
            }
        }
        return max + 1;
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return -1;
        }
    }

    private double readScore(String label) {
        System.out.print("  " + label + " (0-10): ");
        try {
            double score = Double.parseDouble(scanner.nextLine().trim());
            if (score < 0 || score > 10) {
                System.out.println("Error: Score must be between 0 and 10.");
                return -1;
            }
            return score;
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid score.");
            return -1;
        }
    }

    private String generateBar(double percentage) {
        int filled = (int) (percentage / 10);
        int empty = 10 - filled;
        if (filled > 10) filled = 10;
        if (empty < 0) empty = 0;
        return "[" + "#".repeat(filled) + "-".repeat(empty) + "]";
    }

    // =========================================================================
    // Static helpers for other controllers
    // =========================================================================

    public static DesignDepartment findDepartmentById(int id) {
        for (String line : FileManager.readLines(DEPT_FILE)) {
            DesignDepartment d = DesignDepartment.fromCSV(line);
            if (d.getId() == id) return d;
        }
        return null;
    }
}
