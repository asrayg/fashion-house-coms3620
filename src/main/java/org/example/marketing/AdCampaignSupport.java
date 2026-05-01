package org.example.marketing;

import org.example.model.AdCampaign;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/** Parsing, scheduling overlap, console input, and small formatting helpers. */
public final class AdCampaignSupport {

    private AdCampaignSupport() {}

    public static int readInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: Must be a number.");
            return -1;
        }
    }

    public static List<String> parsePlatforms(String input) {
        if (input.isEmpty()) return new ArrayList<>();
        String[] parts = input.split(",");
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String p : parts) {
            String trimmed = p.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;
            if (!seen.add(trimmed)) return null;
            result.add(p.trim());
        }
        return result;
    }

    public static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, AdCampaignConstants.DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static AdCampaign.Status resolveStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(start)) return AdCampaign.Status.PLANNED;
        if (!today.isAfter(end)) return AdCampaign.Status.ACTIVE;
        return AdCampaign.Status.COMPLETED;
    }

    public static String generateBar(double pct) {
        int filled = Math.min(10, (int) (pct / 10));
        return "[" + "#".repeat(filled) + "-".repeat(10 - filled) + "]";
    }

    public static double getTotalSpentForCollection(List<AdCampaign> all, int collectionId) {
        return all.stream()
                .filter(c -> c.getCollectionId() == collectionId
                        && c.getStatus() != AdCampaign.Status.CANCELLED)
                .mapToDouble(AdCampaign::getTotalBudget)
                .sum();
    }

    public static List<String> detectConflicts(List<AdCampaign> all,
                                               int colId,
                                               List<String> platforms,
                                               String start,
                                               String end,
                                               int excludeId) {
        List<String> conflicts = new ArrayList<>();
        for (AdCampaign existing : all) {
            if (existing.getId() == excludeId) continue;
            if (existing.getCollectionId() != colId) continue;
            if (existing.getStatus() == AdCampaign.Status.CANCELLED) continue;

            List<String> existingPlatforms =
                    Arrays.asList(existing.getPlatforms().split("\\|"));
            boolean overlap = platforms.stream()
                    .anyMatch(existingPlatforms::contains);
            if (overlap && datesOverlapRaw(start, end,
                    existing.getStartDate(), existing.getEndDate())) {
                conflicts.add("[" + existing.getId() + "] \"" + existing.getName()
                        + "\" runs " + existing.getStartDate() + " → " + existing.getEndDate());
            }
        }
        return conflicts;
    }

    public static boolean datesOverlap(AdCampaign a, AdCampaign b) {
        return datesOverlapRaw(a.getStartDate(), a.getEndDate(),
                b.getStartDate(), b.getEndDate());
    }

    public static boolean datesOverlapRaw(String s1, String e1, String s2, String e2) {
        LocalDate start1 = parseDate(s1), end1 = parseDate(e1);
        LocalDate start2 = parseDate(s2), end2 = parseDate(e2);
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    public static List<String> sharedPlatforms(AdCampaign a, AdCampaign b) {
        List<String> ap = Arrays.asList(a.getPlatforms().split("\\|"));
        List<String> bp = Arrays.asList(b.getPlatforms().split("\\|"));
        return ap.stream().filter(bp::contains).collect(Collectors.toList());
    }
}
