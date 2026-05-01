package org.example.marketing;

import java.time.format.DateTimeFormatter;

/** Paths and business limits for ad campaigns. */
public final class AdCampaignConstants {

    private AdCampaignConstants() {}

    public static final String CAMPAIGNS_FILE = "data/marketing/campaigns.csv";
    public static final double COLLECTION_BUDGET_LIMIT = 50000.0;
    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
