package org.example.marketing;

import org.example.model.AdCampaign;

import java.util.Scanner;

/** Manual status changes and soft cancel. */
public class CampaignLifecycleService {

    private final Scanner scanner;
    private final AdCampaignRepository repository;

    public CampaignLifecycleService(Scanner scanner, AdCampaignRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public void updateCampaignStatus() {
        System.out.print("Campaign ID: ");
        int id = AdCampaignSupport.readInt(scanner);
        AdCampaign c = repository.findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Error: Cannot change status of a cancelled campaign.");
            return;
        }

        System.out.println("Current status: " + c.getStatus());
        System.out.println("1. PLANNED");
        System.out.println("2. ACTIVE");
        System.out.println("3. PAUSED");
        System.out.println("4. COMPLETED");
        System.out.print("New status: ");

        AdCampaign.Status newStatus;
        switch (scanner.nextLine().trim()) {
            case "1" -> newStatus = AdCampaign.Status.PLANNED;
            case "2" -> newStatus = AdCampaign.Status.ACTIVE;
            case "3" -> newStatus = AdCampaign.Status.PAUSED;
            case "4" -> newStatus = AdCampaign.Status.COMPLETED;
            default  -> {
                System.out.println("Invalid option.");
                return;
            }
        }

        c.setStatus(newStatus);
        repository.update(c);
        System.out.println("Status updated to: " + newStatus);
    }

    public void cancelCampaign() {
        System.out.print("Campaign ID: ");
        int id = AdCampaignSupport.readInt(scanner);
        AdCampaign c = repository.findById(id);
        if (c == null) {
            System.out.println("Error: Campaign not found.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.CANCELLED) {
            System.out.println("Campaign is already cancelled.");
            return;
        }
        if (c.getStatus() == AdCampaign.Status.COMPLETED) {
            System.out.println("Error: Cannot cancel a completed campaign.");
            return;
        }

        System.out.println("Campaign: " + c.getName() + " | Status: " + c.getStatus()
                + " | Budget: $" + String.format("%.2f", c.getTotalBudget()));
        System.out.print("Reason for cancellation (optional): ");
        String reason = scanner.nextLine().trim();
        System.out.print("Confirm cancellation? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        c.setStatus(AdCampaign.Status.CANCELLED);
        if (!reason.isEmpty()) c.setNotes("CANCELLED: " + reason);
        repository.update(c);

        double released = c.getTotalBudget();
        System.out.printf("Campaign cancelled. $%.2f returned to collection budget pool.%n", released);
    }
}
