package org.example.marketing;

import org.example.model.AdCampaign;

import java.util.Scanner;

/**
 * Marketing & Communications — thin menu facade. Business logic lives in sibling
 * services; budget allocation uses the Strategy pattern in {@code marketing.strategy}.
 *
 * <p>Actor: Marketing Manager — Iteration 2/3</p>
 */
public class AdCampaignController {

    /** Backward-compatible alias for the campaigns CSV path. */
    public static final String FILE = AdCampaignConstants.CAMPAIGNS_FILE;

    private final Scanner scanner;
    private final CampaignLaunchService launch;
    private final CampaignQueryService queries;
    private final CampaignLifecycleService lifecycle;
    private final CampaignPerformanceService performance;
    private final CampaignBudgetEditService budgetEdit;
    private final CampaignReportingService reporting;

    public AdCampaignController(Scanner scanner) {
        this.scanner = scanner;
        AdCampaignRepository repository = new AdCampaignRepository();
        this.launch = new CampaignLaunchService(scanner, repository);
        this.queries = new CampaignQueryService(scanner, repository);
        this.lifecycle = new CampaignLifecycleService(scanner, repository);
        this.performance = new CampaignPerformanceService(scanner, repository);
        this.budgetEdit = new CampaignBudgetEditService(scanner, repository);
        this.reporting = new CampaignReportingService(scanner, repository);
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║     Marketing & Communications Management    ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Launch Ad Campaign                     ║");
            System.out.println("║  2.  List All Campaigns                     ║");
            System.out.println("║  3.  View Campaign Details                  ║");
            System.out.println("║  4.  Update Campaign Status                 ║");
            System.out.println("║  5.  Record Campaign Performance            ║");
            System.out.println("║  6.  Edit Campaign Platforms & Budget       ║");
            System.out.println("║  7.  Cancel Campaign                        ║");
            System.out.println("║  8.  Collection Budget Report               ║");
            System.out.println("║  9.  Platform Performance Analysis          ║");
            System.out.println("║ 10.  Campaign ROI Dashboard                 ║");
            System.out.println("║ 11.  Scheduling Conflict Checker            ║");
            System.out.println("║ 12.  Sync Campaign Statuses (auto-update)   ║");
            System.out.println("║ 13.  Campaign Audit Trail                   ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> launch.launchCampaign();
                case "2"  -> queries.listCampaigns();
                case "3"  -> queries.viewCampaignDetails();
                case "4"  -> lifecycle.updateCampaignStatus();
                case "5"  -> performance.recordPerformance();
                case "6"  -> budgetEdit.editCampaignBudget();
                case "7"  -> lifecycle.cancelCampaign();
                case "8"  -> reporting.collectionBudgetReport();
                case "9"  -> reporting.platformPerformanceAnalysis();
                case "10" -> reporting.campaignRoiDashboard();
                case "11" -> reporting.schedulingConflictChecker();
                case "12" -> reporting.syncCampaignStatuses();
                case "13" -> reporting.campaignAuditTrail();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    public static AdCampaign findById(int id) {
        return new AdCampaignRepository().findById(id);
    }
}
