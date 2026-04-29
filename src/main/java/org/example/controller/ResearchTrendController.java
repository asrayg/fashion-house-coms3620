package org.example.controller;

import org.example.model.CompetitorObservation;
import org.example.model.MarketTrend;
import org.example.model.TrendAlert;
import org.example.model.TrendForecast;
import org.example.observer.DesignDeptObserver;
import org.example.observer.MarketingDeptObserver;
import org.example.observer.ProductionDeptObserver;
import org.example.observer.TrendObserver;
import org.example.observer.TrendSubject;
import org.example.util.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Research & Trend Analysis Department Management — Iteration 3
 * Actor: Research Analyst
 * Code Owner: Billy Dang
 *
 * Primary design pattern: OBSERVER (GoF behavioral)
 *  - This controller is the concrete TrendSubject.
 *  - DesignDeptObserver, MarketingDeptObserver, ProductionDeptObserver are
 *    concrete TrendObservers, registered at construction time.
 *  - When a MarketTrend is logged, notifyObservers(trend) fans out to every
 *    registered observer; each independently filters and persists a TrendAlert.
 *
 * Secondary design pattern: STRATEGY (GoF behavioral)
 *  - Each observer composes a TrendRelevanceStrategy that decides relevance,
 *    priority, and message text. Swapping a strategy changes department
 *    behavior without modifying the observer or this controller.
 *
 * Features:
 *  - Log Market Trend (main use case — fires Observer fan-out)
 *  - List / view / archive trends with lifecycle stages
 *  - Update trend lifecycle stage and confidence (post-logging revisions)
 *  - Subscribe / unsubscribe departments at runtime
 *  - Generate per-trend forecast (peak season, decline season, demand lift)
 *  - Record and list competitor observations (supporting evidence)
 *  - View, filter and acknowledge trend alerts per department
 *  - Trend performance dashboard (counts by category, lifecycle, top sources)
 *  - Notification audit trail (every fan-out event recorded)
 *  - Seed sample data on first run for demo purposes
 */
public class ResearchTrendController implements TrendSubject {

    public  static final String TRENDS_FILE      = "data/research/market_trends.csv";
    public  static final String ALERTS_FILE      = "data/research/trend_alerts.csv";
    public  static final String FORECASTS_FILE   = "data/research/trend_forecasts.csv";
    public  static final String COMPETITOR_FILE  = "data/research/competitor_observations.csv";
    public  static final String AUDIT_FILE       = "data/research/notification_audit.csv";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MIN_CONFIDENCE = 1;
    private static final int MAX_CONFIDENCE = 5;

    private final Scanner scanner;
    private final List<TrendObserver> observers = new ArrayList<>();

    public ResearchTrendController(Scanner scanner) {
        this.scanner = scanner;
        // Default subscribers — can be revoked through the menu at runtime.
        addObserver(new DesignDeptObserver());
        addObserver(new MarketingDeptObserver());
        addObserver(new ProductionDeptObserver());
        seedDataIfEmpty();
    }

    // =========================================================================
    // TrendSubject (Observer pattern — subject side)
    // =========================================================================

    @Override
    public void addObserver(TrendObserver observer) {
        for (TrendObserver existing : observers) {
            if (existing.getDepartmentName().equalsIgnoreCase(observer.getDepartmentName())) return;
        }
        observers.add(observer);
    }

    @Override
    public void removeObserver(TrendObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(MarketTrend trend) {
        String today = LocalDate.now().toString();
        for (TrendObserver o : observers) {
            o.onTrendLogged(trend);
            FileManager.appendLine(AUDIT_FILE,
                FileManager.nextId(AUDIT_FILE) + "," + trend.getId() + ","
                + o.getDepartmentName() + "," + today + ",NOTIFIED");
        }
    }

    // =========================================================================
    // Main Menu
    // =========================================================================

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║   Research & Trend Analysis — Billy         ║");
            System.out.println("╠══════════════════════════════════════════════╣");
            System.out.println("║  1.  Log Market Trend                       ║");
            System.out.println("║  2.  List All Trends                        ║");
            System.out.println("║  3.  View Trend Details                     ║");
            System.out.println("║  4.  Update Trend Lifecycle Stage           ║");
            System.out.println("║  5.  Archive Trend (soft)                   ║");
            System.out.println("║  6.  Generate Trend Forecast                ║");
            System.out.println("║  7.  View Forecasts                         ║");
            System.out.println("║  8.  Record Competitor Observation          ║");
            System.out.println("║  9.  List Competitor Observations           ║");
            System.out.println("║ 10.  View Trend Alerts (filter by dept)     ║");
            System.out.println("║ 11.  Acknowledge Alert                      ║");
            System.out.println("║ 12.  Subscribe / Unsubscribe Department     ║");
            System.out.println("║ 13.  Trend Performance Dashboard            ║");
            System.out.println("║ 14.  Notification Audit Trail               ║");
            System.out.println("║  0.  Back                                   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.print("Select: ");
            switch (scanner.nextLine().trim()) {
                case "1"  -> logMarketTrend();
                case "2"  -> listAllTrends();
                case "3"  -> viewTrendDetails();
                case "4"  -> updateLifecycleStage();
                case "5"  -> archiveTrend();
                case "6"  -> generateForecast();
                case "7"  -> viewForecasts();
                case "8"  -> recordCompetitorObservation();
                case "9"  -> listCompetitorObservations();
                case "10" -> viewAlerts();
                case "11" -> acknowledgeAlert();
                case "12" -> manageSubscriptions();
                case "13" -> performanceDashboard();
                case "14" -> auditTrail();
                case "0"  -> back = true;
                default   -> System.out.println("Invalid option.");
            }
        }
    }

    // =========================================================================
    // 1. Log Market Trend  (Main Use Case — fires the Observer fan-out)
    // =========================================================================

    /**
     * UC: Log Market Trend
     * Actor: Research Analyst
     * Goal: Persist a market trend record and notify every subscribed
     *       department so they can act on the new information.
     * Pattern role: ResearchTrendController is the Subject; observers are
     *               notified via notifyObservers() after persistence succeeds.
     */
    public void logMarketTrend() {
        System.out.println("\n--- Log Market Trend ---");

        // Step 1: Category
        System.out.println("Categories: FASHION, COLOR, FABRIC, SILHOUETTE, CONSUMER_BEHAVIOR");
        System.out.print("Category: ");
        String catInput = scanner.nextLine().trim().toUpperCase();
        MarketTrend.Category category;
        try {
            category = MarketTrend.Category.valueOf(catInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: invalid category. Trend not logged.");
            return;
        }

        // Step 2: Name
        System.out.print("Trend name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: name is required.");
            return;
        }

        // Step 3: Season + duplicate check
        System.out.print("Target season (e.g. Spring2026, Fall2026): ");
        String season = scanner.nextLine().trim();
        if (season.isEmpty()) {
            System.out.println("Error: season is required.");
            return;
        }
        for (MarketTrend existing : loadAllTrends()) {
            if (existing.getName().equalsIgnoreCase(name)
                    && existing.getSeason().equalsIgnoreCase(season)
                    && existing.getStatus() == MarketTrend.Status.ACTIVE) {
                System.out.println("Warning: an active trend '" + name + "' for '" + season
                        + "' already exists (ID " + existing.getId() + ").");
                System.out.print("Log anyway? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    System.out.println("Aborted.");
                    return;
                }
                break;
            }
        }

        // Step 4: Region
        System.out.print("Target region (Global, North America, Europe, Asia, Other): ");
        String region = scanner.nextLine().trim();
        if (region.isEmpty()) region = "Global";

        // Step 5: Description
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        // Step 6: Source
        System.out.print("Source (e.g. Vogue, Instagram, Field Survey): ");
        String source = scanner.nextLine().trim();
        if (source.isEmpty()) source = "Internal";

        // Step 7: Confidence (validated)
        int confidence = readBoundedInt("Confidence level (" + MIN_CONFIDENCE + "–" + MAX_CONFIDENCE + "): ",
                                        MIN_CONFIDENCE, MAX_CONFIDENCE);
        if (confidence == -1) {
            System.out.println("Error: invalid confidence level. Trend not logged.");
            return;
        }

        // Step 8: Lifecycle stage
        System.out.println("Lifecycle stages: EMERGING, RISING, PEAK, DECLINING, FADED");
        System.out.print("Lifecycle stage [default EMERGING]: ");
        String stageInput = scanner.nextLine().trim().toUpperCase();
        MarketTrend.LifecycleStage stage;
        if (stageInput.isEmpty()) {
            stage = MarketTrend.LifecycleStage.EMERGING;
        } else {
            try {
                stage = MarketTrend.LifecycleStage.valueOf(stageInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: invalid lifecycle stage. Trend not logged.");
                return;
            }
        }

        // Step 9: Optional collection link
        System.out.print("Related collection ID (Enter for none): ");
        String collIdInput = scanner.nextLine().trim();
        int collectionId = -1;
        if (!collIdInput.isEmpty()) {
            try {
                collectionId = Integer.parseInt(collIdInput);
            } catch (NumberFormatException e) {
                System.out.println("Error: collection ID must be numeric. Trend not logged.");
                return;
            }
        }

        // Step 10: Persist
        int id = FileManager.nextId(TRENDS_FILE);
        MarketTrend trend = new MarketTrend(
            id, category, name, description, source, confidence,
            season, region, stage, collectionId,
            LocalDate.now().toString(), MarketTrend.Status.ACTIVE);
        FileManager.appendLine(TRENDS_FILE, trend.toCSV());

        // Step 11: Observer fan-out
        System.out.println("\nTrend logged successfully — ID: " + id);
        System.out.println("Notifying " + observers.size() + " registered observer(s)...");
        notifyObservers(trend);
        System.out.println("Done.");
    }

    // =========================================================================
    // 2. List All Trends
    // =========================================================================

    private void listAllTrends() {
        List<MarketTrend> trends = loadAllTrends();
        if (trends.isEmpty()) {
            System.out.println("No trends logged yet.");
            return;
        }
        System.out.println("\n--- All Market Trends (" + trends.size() + ") ---");
        for (MarketTrend t : trends) {
            System.out.println(t);
            System.out.println();
        }
    }

    // =========================================================================
    // 3. View Trend Details (with linked alerts and forecasts)
    // =========================================================================

    private void viewTrendDetails() {
        System.out.print("Trend ID: ");
        int id = readInt();
        if (id == -1) return;
        MarketTrend t = findTrendById(id);
        if (t == null) {
            System.out.println("Trend not found.");
            return;
        }
        System.out.println("\n--- Trend Details ---");
        System.out.println(t);

        System.out.println("\n  Alerts generated:");
        boolean anyAlert = false;
        for (TrendAlert a : loadAllAlerts()) {
            if (a.getTrendId() == id) {
                System.out.println("    " + a);
                anyAlert = true;
            }
        }
        if (!anyAlert) System.out.println("    (none)");

        System.out.println("\n  Forecasts attached:");
        boolean anyForecast = false;
        for (TrendForecast f : loadAllForecasts()) {
            if (f.getTrendId() == id) {
                System.out.println("    " + f);
                anyForecast = true;
            }
        }
        if (!anyForecast) System.out.println("    (none)");
    }

    // =========================================================================
    // 4. Update Lifecycle Stage
    // =========================================================================

    private void updateLifecycleStage() {
        System.out.print("Trend ID: ");
        int id = readInt();
        if (id == -1) return;
        MarketTrend t = findTrendById(id);
        if (t == null) {
            System.out.println("Trend not found.");
            return;
        }
        System.out.println("Current lifecycle: " + t.getLifecycleStage());
        System.out.println("Stages: EMERGING, RISING, PEAK, DECLINING, FADED");
        System.out.print("New stage: ");
        String input = scanner.nextLine().trim().toUpperCase();
        MarketTrend.LifecycleStage stage;
        try {
            stage = MarketTrend.LifecycleStage.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: invalid lifecycle stage.");
            return;
        }
        t.setLifecycleStage(stage);
        saveAllTrends(replaceTrend(loadAllTrends(), t));
        System.out.println("Lifecycle updated to " + stage + ".");
    }

    // =========================================================================
    // 5. Archive Trend (soft delete)
    // =========================================================================

    private void archiveTrend() {
        System.out.print("Trend ID to archive: ");
        int id = readInt();
        if (id == -1) return;
        MarketTrend t = findTrendById(id);
        if (t == null) {
            System.out.println("Trend not found.");
            return;
        }
        if (t.getStatus() == MarketTrend.Status.ARCHIVED) {
            System.out.println("Trend is already archived.");
            return;
        }
        t.setStatus(MarketTrend.Status.ARCHIVED);
        saveAllTrends(replaceTrend(loadAllTrends(), t));
        System.out.println("Trend " + id + " archived (record preserved).");
    }

    // =========================================================================
    // 6. Generate Trend Forecast
    // =========================================================================

    private void generateForecast() {
        System.out.print("Trend ID: ");
        int id = readInt();
        if (id == -1) return;
        MarketTrend t = findTrendById(id);
        if (t == null) {
            System.out.println("Trend not found.");
            return;
        }

        // Heuristic: expected demand lift scales with confidence and lifecycle
        double base = t.getConfidenceLevel() * 5.0;       // 5..25
        double lift = base + lifecycleBoost(t.getLifecycleStage());
        String peak = projectedPeak(t.getSeason(), t.getLifecycleStage());
        String decline = projectedDecline(peak);
        String recommendation = buildRecommendation(t, lift);

        TrendForecast f = new TrendForecast(
            FileManager.nextId(FORECASTS_FILE),
            id, peak, decline, lift, recommendation,
            LocalDate.now().toString());
        FileManager.appendLine(FORECASTS_FILE, f.toCSV());
        System.out.println("Forecast created:");
        System.out.println("  " + f);
    }

    // =========================================================================
    // 7. View Forecasts
    // =========================================================================

    private void viewForecasts() {
        List<TrendForecast> forecasts = loadAllForecasts();
        if (forecasts.isEmpty()) {
            System.out.println("No forecasts yet.");
            return;
        }
        System.out.println("\n--- Trend Forecasts (" + forecasts.size() + ") ---");
        for (TrendForecast f : forecasts) System.out.println(f);
    }

    // =========================================================================
    // 8. Record Competitor Observation
    // =========================================================================

    private void recordCompetitorObservation() {
        System.out.println("\n--- Record Competitor Observation ---");
        System.out.print("Competitor brand: ");
        String brand = scanner.nextLine().trim();
        if (brand.isEmpty()) {
            System.out.println("Error: brand is required.");
            return;
        }
        System.out.print("Activity type (ProductLaunch / PriceCut / MarketingShift / Other): ");
        String activity = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("Impact level (LOW, MEDIUM, HIGH): ");
        CompetitorObservation.ImpactLevel impact;
        try {
            impact = CompetitorObservation.ImpactLevel.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: invalid impact level.");
            return;
        }
        CompetitorObservation obs = new CompetitorObservation(
            FileManager.nextId(COMPETITOR_FILE),
            brand, activity, desc, LocalDate.now().toString(), impact);
        FileManager.appendLine(COMPETITOR_FILE, obs.toCSV());
        System.out.println("Observation recorded (ID " + obs.getId() + ").");
    }

    // =========================================================================
    // 9. List Competitor Observations
    // =========================================================================

    private void listCompetitorObservations() {
        List<CompetitorObservation> obs = loadAllCompetitors();
        if (obs.isEmpty()) {
            System.out.println("No competitor observations yet.");
            return;
        }
        System.out.println("\n--- Competitor Observations (" + obs.size() + ") ---");
        for (CompetitorObservation o : obs) System.out.println(o);
    }

    // =========================================================================
    // 10. View Alerts (filterable by department)
    // =========================================================================

    private void viewAlerts() {
        System.out.print("Filter by department (Design/Marketing/Production, Enter for all): ");
        String filter = scanner.nextLine().trim();
        List<TrendAlert> alerts = loadAllAlerts();
        if (alerts.isEmpty()) {
            System.out.println("No alerts on file.");
            return;
        }
        System.out.println("\n--- Trend Alerts ---");
        int shown = 0;
        for (TrendAlert a : alerts) {
            if (filter.isEmpty() || a.getDepartment().equalsIgnoreCase(filter)) {
                System.out.println(a);
                shown++;
            }
        }
        if (shown == 0) System.out.println("(no alerts match filter)");
    }

    // =========================================================================
    // 11. Acknowledge Alert
    // =========================================================================

    private void acknowledgeAlert() {
        System.out.print("Alert ID to acknowledge: ");
        int id = readInt();
        if (id == -1) return;
        List<TrendAlert> alerts = loadAllAlerts();
        boolean found = false;
        for (TrendAlert a : alerts) {
            if (a.getId() == id) {
                if (a.isAcknowledged()) {
                    System.out.println("Alert is already acknowledged.");
                    return;
                }
                a.setAcknowledged(true);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Alert not found.");
            return;
        }
        List<String> rewritten = new ArrayList<>();
        for (TrendAlert a : alerts) rewritten.add(a.toCSV());
        FileManager.writeLines(ALERTS_FILE, rewritten);
        System.out.println("Alert " + id + " acknowledged.");
    }

    // =========================================================================
    // 12. Subscribe / Unsubscribe Department (manage observer registry)
    // =========================================================================

    private void manageSubscriptions() {
        System.out.println("\n--- Subscriptions ---");
        if (observers.isEmpty()) {
            System.out.println("(no subscribers)");
        } else {
            for (TrendObserver o : observers) {
                System.out.println("  - " + o.getDepartmentName());
            }
        }
        System.out.print("Action (subscribe / unsubscribe / cancel): ");
        String action = scanner.nextLine().trim().toLowerCase();
        if (action.equals("cancel") || action.isEmpty()) return;

        System.out.print("Department (Design / Marketing / Production): ");
        String dept = scanner.nextLine().trim().toLowerCase();

        TrendObserver newObserver = switch (dept) {
            case "design"     -> new DesignDeptObserver();
            case "marketing"  -> new MarketingDeptObserver();
            case "production" -> new ProductionDeptObserver();
            default           -> null;
        };
        if (newObserver == null) {
            System.out.println("Unknown department.");
            return;
        }

        if (action.equals("subscribe")) {
            int before = observers.size();
            addObserver(newObserver);
            if (observers.size() == before) {
                System.out.println(newObserver.getDepartmentName() + " was already subscribed.");
            } else {
                System.out.println(newObserver.getDepartmentName() + " subscribed.");
            }
        } else if (action.equals("unsubscribe")) {
            TrendObserver match = null;
            for (TrendObserver o : observers) {
                if (o.getDepartmentName().equalsIgnoreCase(newObserver.getDepartmentName())) {
                    match = o;
                    break;
                }
            }
            if (match == null) {
                System.out.println(newObserver.getDepartmentName() + " was not subscribed.");
            } else {
                removeObserver(match);
                System.out.println(newObserver.getDepartmentName() + " unsubscribed.");
            }
        } else {
            System.out.println("Unknown action.");
        }
    }

    // =========================================================================
    // 13. Trend Performance Dashboard
    // =========================================================================

    private void performanceDashboard() {
        List<MarketTrend> trends = loadAllTrends();
        if (trends.isEmpty()) {
            System.out.println("Dashboard unavailable: no trends logged yet.");
            return;
        }
        Map<MarketTrend.Category, Integer> byCategory       = new HashMap<>();
        Map<MarketTrend.LifecycleStage, Integer> byLifecycle = new HashMap<>();
        Map<String, Integer> bySource                       = new HashMap<>();
        int active = 0;
        int archived = 0;
        double avgConfidence = 0.0;

        for (MarketTrend t : trends) {
            byCategory.merge(t.getCategory(), 1, Integer::sum);
            byLifecycle.merge(t.getLifecycleStage(), 1, Integer::sum);
            bySource.merge(t.getSource(), 1, Integer::sum);
            if (t.getStatus() == MarketTrend.Status.ACTIVE) active++; else archived++;
            avgConfidence += t.getConfidenceLevel();
        }
        avgConfidence /= trends.size();

        List<TrendAlert> alerts = loadAllAlerts();
        int ackCount = 0;
        for (TrendAlert a : alerts) if (a.isAcknowledged()) ackCount++;

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║       Trend Performance Dashboard           ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println("Total trends:         " + trends.size()
                + "  (active " + active + ", archived " + archived + ")");
        System.out.printf("Average confidence:   %.2f / 5%n", avgConfidence);
        System.out.println("Subscribed observers: " + observers.size());
        System.out.println();
        System.out.println("By category:");
        for (Map.Entry<MarketTrend.Category, Integer> e : byCategory.entrySet()) {
            System.out.println("  " + pad(e.getKey().name(), 20) + e.getValue());
        }
        System.out.println();
        System.out.println("By lifecycle stage:");
        for (Map.Entry<MarketTrend.LifecycleStage, Integer> e : byLifecycle.entrySet()) {
            System.out.println("  " + pad(e.getKey().name(), 20) + e.getValue());
        }
        System.out.println();
        System.out.println("Top sources:");
        bySource.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(3)
            .forEach(e -> System.out.println("  " + pad(e.getKey(), 24) + e.getValue()));
        System.out.println();
        System.out.println("Alerts: " + alerts.size()
                + " (acknowledged " + ackCount + ", pending " + (alerts.size() - ackCount) + ")");
    }

    // =========================================================================
    // 14. Notification Audit Trail
    // =========================================================================

    private void auditTrail() {
        List<String> rows = FileManager.readLines(AUDIT_FILE);
        if (rows.isEmpty()) {
            System.out.println("No notification events recorded yet.");
            return;
        }
        System.out.println("\n--- Notification Audit Trail ---");
        System.out.println("auditId,trendId,department,date,event");
        for (String row : rows) System.out.println("  " + row);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private List<MarketTrend> loadAllTrends() {
        List<MarketTrend> out = new ArrayList<>();
        for (String row : FileManager.readLines(TRENDS_FILE)) {
            try { out.add(MarketTrend.fromCSV(row)); }
            catch (Exception ignored) { /* skip malformed */ }
        }
        return out;
    }

    private void saveAllTrends(List<MarketTrend> trends) {
        List<String> lines = new ArrayList<>();
        for (MarketTrend t : trends) lines.add(t.toCSV());
        FileManager.writeLines(TRENDS_FILE, lines);
    }

    private List<TrendAlert> loadAllAlerts() {
        List<TrendAlert> out = new ArrayList<>();
        for (String row : FileManager.readLines(ALERTS_FILE)) {
            try { out.add(TrendAlert.fromCSV(row)); }
            catch (Exception ignored) { /* skip malformed */ }
        }
        return out;
    }

    private List<TrendForecast> loadAllForecasts() {
        List<TrendForecast> out = new ArrayList<>();
        for (String row : FileManager.readLines(FORECASTS_FILE)) {
            try { out.add(TrendForecast.fromCSV(row)); }
            catch (Exception ignored) { /* skip malformed */ }
        }
        return out;
    }

    private List<CompetitorObservation> loadAllCompetitors() {
        List<CompetitorObservation> out = new ArrayList<>();
        for (String row : FileManager.readLines(COMPETITOR_FILE)) {
            try { out.add(CompetitorObservation.fromCSV(row)); }
            catch (Exception ignored) { /* skip malformed */ }
        }
        return out;
    }

    private MarketTrend findTrendById(int id) {
        for (MarketTrend t : loadAllTrends()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    private List<MarketTrend> replaceTrend(List<MarketTrend> trends, MarketTrend updated) {
        List<MarketTrend> out = new ArrayList<>();
        for (MarketTrend t : trends) {
            out.add(t.getId() == updated.getId() ? updated : t);
        }
        return out;
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: must be numeric.");
            return -1;
        }
    }

    private int readBoundedInt(String prompt, int min, int max) {
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v < min || v > max) {
                    System.out.println("Must be between " + min + " and " + max + ".");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Enter a whole number.");
            }
        }
        return -1;
    }

    private double lifecycleBoost(MarketTrend.LifecycleStage stage) {
        switch (stage) {
            case EMERGING:  return  5.0;
            case RISING:    return 15.0;
            case PEAK:      return 25.0;
            case DECLINING: return -5.0;
            case FADED:     return -15.0;
            default:        return  0.0;
        }
    }

    private String projectedPeak(String season, MarketTrend.LifecycleStage stage) {
        // Crude projection: PEAK trends peak in the same season; otherwise next season
        if (stage == MarketTrend.LifecycleStage.PEAK)     return season;
        if (stage == MarketTrend.LifecycleStage.RISING)   return season;
        if (stage == MarketTrend.LifecycleStage.EMERGING) return nextSeason(season);
        return season;
    }

    private String projectedDecline(String peakSeason) {
        return nextSeason(peakSeason);
    }

    private String nextSeason(String season) {
        // Toggle Spring↔Fall, advance year on Fall→Spring; pure heuristic
        if (season == null || season.isBlank()) return "Unknown";
        if (season.toLowerCase().startsWith("spring")) return "Fall" + extractYear(season);
        if (season.toLowerCase().startsWith("fall"))   return "Spring" + (extractYearInt(season) + 1);
        return season + "+1";
    }

    private String extractYear(String season) {
        StringBuilder digits = new StringBuilder();
        for (char c : season.toCharArray()) if (Character.isDigit(c)) digits.append(c);
        return digits.length() == 0 ? "" : digits.toString();
    }

    private int extractYearInt(String season) {
        try { return Integer.parseInt(extractYear(season)); }
        catch (NumberFormatException e) { return 2026; }
    }

    private String buildRecommendation(MarketTrend t, double lift) {
        if (lift >= 30) {
            return "Strong signal — recommend Design + Production prioritize " + t.getCategory();
        }
        if (lift >= 15) {
            return "Moderate signal — Marketing should plan supporting campaign";
        }
        if (lift > 0) {
            return "Weak signal — monitor for one cycle before acting";
        }
        return "Negative signal — deprioritize associated work";
    }

    private String pad(String s, int width) {
        if (s.length() >= width) return s + "  ";
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    // =========================================================================
    // Seed sample data on first run (demo aid)
    // =========================================================================

    private void seedDataIfEmpty() {
        if (FileManager.hasRecords(TRENDS_FILE)) return;

        List<String> trends = new ArrayList<>();
        trends.add(new MarketTrend(1, MarketTrend.Category.COLOR, "Butter Yellow",
                "Soft buttery yellow trending heavily across luxury and mid-market spring lines",
                "Vogue April 2026", 5, "Spring2026", "Global",
                MarketTrend.LifecycleStage.PEAK, -1,
                LocalDate.now().toString(), MarketTrend.Status.ACTIVE).toCSV());
        trends.add(new MarketTrend(2, MarketTrend.Category.SILHOUETTE, "Drop-Waist Dresses",
                "Drop-waist silhouettes regaining popularity, driven by 1920s revival",
                "Pinterest Trend Report", 4, "Fall2026", "North America",
                MarketTrend.LifecycleStage.RISING, -1,
                LocalDate.now().toString(), MarketTrend.Status.ACTIVE).toCSV());
        trends.add(new MarketTrend(3, MarketTrend.Category.FABRIC, "Recycled Polyester Blends",
                "Sustainability-driven push toward recycled poly blends in core basics",
                "Sourcing Journal", 4, "Fall2026", "Global",
                MarketTrend.LifecycleStage.RISING, -1,
                LocalDate.now().toString(), MarketTrend.Status.ACTIVE).toCSV());
        trends.add(new MarketTrend(4, MarketTrend.Category.CONSUMER_BEHAVIOR, "Quiet Luxury Demand",
                "Sustained shift toward unbranded, high-quality basics; cap on logo placement",
                "Field Survey", 3, "Spring2026", "Europe",
                MarketTrend.LifecycleStage.EMERGING, -1,
                LocalDate.now().toString(), MarketTrend.Status.ACTIVE).toCSV());
        FileManager.writeLines(TRENDS_FILE, trends);

        List<String> competitors = new ArrayList<>();
        competitors.add(new CompetitorObservation(1, "Brand A", "ProductLaunch",
                "Launched butter-yellow capsule collection — sold out in 48h",
                LocalDate.now().toString(), CompetitorObservation.ImpactLevel.HIGH).toCSV());
        competitors.add(new CompetitorObservation(2, "Brand B", "MarketingShift",
                "Pivoted seasonal campaign to highlight recycled materials sourcing",
                LocalDate.now().toString(), CompetitorObservation.ImpactLevel.MEDIUM).toCSV());
        FileManager.writeLines(COMPETITOR_FILE, competitors);
    }
}
