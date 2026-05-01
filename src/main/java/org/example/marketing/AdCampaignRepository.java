package org.example.marketing;

import org.example.model.AdCampaign;
import org.example.util.FileManager;

import java.util.ArrayList;
import java.util.List;

/** CSV persistence for {@link AdCampaign}. */
public class AdCampaignRepository {

    public List<AdCampaign> loadAll() {
        List<AdCampaign> list = new ArrayList<>();
        for (String line : FileManager.readLines(AdCampaignConstants.CAMPAIGNS_FILE)) {
            if (line.trim().isEmpty()) continue;
            list.add(AdCampaign.fromCSV(line));
        }
        return list;
    }

    public void append(AdCampaign campaign) {
        FileManager.appendLine(AdCampaignConstants.CAMPAIGNS_FILE, campaign.toCSV());
    }

    public void update(AdCampaign updated) {
        List<String> lines = FileManager.readLines(AdCampaignConstants.CAMPAIGNS_FILE);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) continue;
            if (AdCampaign.fromCSV(lines.get(i)).getId() == updated.getId()) {
                lines.set(i, updated.toCSV());
                break;
            }
        }
        FileManager.writeLines(AdCampaignConstants.CAMPAIGNS_FILE, lines);
    }

    public AdCampaign findById(int id) {
        for (String line : FileManager.readLines(AdCampaignConstants.CAMPAIGNS_FILE)) {
            if (line.trim().isEmpty()) continue;
            AdCampaign c = AdCampaign.fromCSV(line);
            if (c.getId() == id) return c;
        }
        return null;
    }
}
