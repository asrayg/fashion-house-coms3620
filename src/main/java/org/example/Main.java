package org.example;

import org.example.controller.*;
import org.example.sales.SalesRetailController;

import java.util.Scanner;

/**
 * Fashion House Management System — Entry Point
 *
 * Console-based, file-backed. No GUI, no login required.
 * Run from the project root so relative paths (data/*.csv) resolve correctly.
 */
public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("============================================");
        System.out.println("   Fashion House Management System v1.0    ");
        System.out.println("============================================");

        ProductionAllocationController productionAllocationController = new ProductionAllocationController(scanner);
        MaterialConsumptionController materialConsumptionController = new MaterialConsumptionController(scanner);
        QualityCheckpointController qualityCheckpointController = new QualityCheckpointController(scanner);
        ProductionScheduleController productionScheduleController = new ProductionScheduleController(scanner);
        PerformanceMetricController performanceMetricController = new PerformanceMetricController(scanner);

        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Collection Management       (UC1)");
            System.out.println("2. Garment Design              (UC2)");
            System.out.println("3. Product Specifications      (UC3)");
            System.out.println("4. Material Registry           (UC4)");
            System.out.println("5. Material Orders             (UC5)");
            System.out.println("6. Design Department           (Iter2)");
            System.out.println("7. Marketing & Campaigns       (Iter2)");
            System.out.println("\n=== PRODUCTION DEPARTMENT (Iteration 2) ===");
            System.out.println("8.  Allocate Garment to Production");
            System.out.println("9.  Track Material Consumption");
            System.out.println("10. Manage Quality Checkpoints");
            System.out.println("11. Plan Production Schedule");
            System.out.println("12. Generate Performance Report");
            System.out.println("=== END OF PRODUCTION DEPARTMENT ===");          
            System.out.println("13. Sales & Retail             (UC9 - Iter2)");
            System.out.println("0. Exit");
            System.out.print("Select: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> new CollectionController(scanner).menu();
                case "2" -> new GarmentDesignController(scanner).menu();
                case "3" -> new ProductSpecificationController(scanner).menu();
                case "4" -> new MaterialController(scanner).menu();
                case "5" -> new MaterialOrderController(scanner).menu();
                case "6" -> new DesignDepartmentController(scanner).menu();
                case "7" -> new AdCampaignController(scanner).menu();
                case "8" -> productionAllocationController.allocateGarmentToProduction();
                case "9" -> {
                    productionAllocationController.viewProductionAllocations();
                    System.out.println("\n1. Track New Consumption\n2. View All Records\n3. View By Allocation");
                    System.out.print("Choice: ");
                    int consumptionChoice = Integer.parseInt(scanner.nextLine());
                    switch (consumptionChoice) {
                        case 1 -> materialConsumptionController.trackMaterialConsumption();
                        case 2 -> materialConsumptionController.viewConsumptionRecords();
                        case 3 -> materialConsumptionController.viewByAllocation();
                        default -> System.out.println("Invalid option. Try again.");
                    }
                }
                case "10" -> {
                    System.out.println("\n1. Perform Quality Checkpoint\n2. View All Checkpoints\n3. View By Batch");
                    System.out.print("Choice: ");
                    int qualityChoice = Integer.parseInt(scanner.nextLine());
                    switch (qualityChoice) {
                        case 1 -> qualityCheckpointController.performQualityCheckpoint();
                        case 2 -> qualityCheckpointController.viewQualityCheckpoints();
                        case 3 -> qualityCheckpointController.viewByBatch();
                        default -> System.out.println("Invalid option. Try again.");
                    }
                }
                case "11" -> {
                    System.out.println("\n1. Create Schedule\n2. View Schedules\n3. Update Schedule");
                    System.out.print("Choice: ");
                    int scheduleChoice = Integer.parseInt(scanner.nextLine());
                    switch (scheduleChoice) {
                        case 1 -> productionScheduleController.createProductionSchedule();
                        case 2 -> productionScheduleController.viewSchedules();
                        case 3 -> productionScheduleController.updateSchedule();
                        default -> System.out.println("Invalid option. Try again.");
                    }
                }
                case "12" -> {
                    System.out.println("\n1. Generate Report\n2. View Reports");
                    System.out.print("Choice: ");
                    int reportChoice = Integer.parseInt(scanner.nextLine());
                    switch (reportChoice) {
                        case 1 -> performanceMetricController.generatePerformanceReport();
                        case 2 -> performanceMetricController.viewPerformanceReports();
                        default -> System.out.println("Invalid option. Try again.");
                    }
                }
                case "13" -> new SalesRetailController(scanner).menu();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option. Try again.");
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }
}
