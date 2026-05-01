package org.example.model;

/**
 * A wholesale account (boutique, department store, online retailer, distributor)
 * that the fashion house sells to in bulk.
 *
 * CSV format: id,name,partnerType,contactPerson,contactEmail,region,
 *             departmentId,accountStatus,annualVolume
 */
public class WholesalePartner {

    public enum AccountStatus { PROSPECT, ACTIVE, SUSPENDED, TERMINATED }

    private int id;
    private String name;
    private String partnerType;
    private String contactPerson;
    private String contactEmail;
    private String region;
    private int departmentId;
    private AccountStatus accountStatus;
    private double annualVolume;

    public WholesalePartner(int id, String name, String partnerType, String contactPerson,
                            String contactEmail, String region, int departmentId,
                            AccountStatus accountStatus, double annualVolume) {
        this.id = id;
        this.name = name;
        this.partnerType = partnerType;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.region = region;
        this.departmentId = departmentId;
        this.accountStatus = accountStatus;
        this.annualVolume = annualVolume;
    }

    public String toCSV() {
        return id + "," + name + "," + partnerType + "," + contactPerson + ","
             + contactEmail + "," + region + "," + departmentId + ","
             + accountStatus.name() + "," + annualVolume;
    }

    public static WholesalePartner fromCSV(String line) {
        String[] parts = line.split(",", 9);
        return new WholesalePartner(
            Integer.parseInt(parts[0].trim()),
            parts[1].trim(),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            parts[5].trim(),
            Integer.parseInt(parts[6].trim()),
            AccountStatus.valueOf(parts[7].trim()),
            Double.parseDouble(parts[8].trim())
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " (" + partnerType + ")"
             + " | Contact: " + contactPerson + " <" + contactEmail + ">"
             + " | Region: " + region
             + " | DeptID: " + departmentId
             + " | Status: " + accountStatus
             + " | Annual Volume: $" + String.format("%.2f", annualVolume);
    }

    public int getId()                       { return id; }
    public String getName()                  { return name; }
    public String getPartnerType()           { return partnerType; }
    public String getContactPerson()         { return contactPerson; }
    public String getContactEmail()          { return contactEmail; }
    public String getRegion()                { return region; }
    public int getDepartmentId()             { return departmentId; }
    public AccountStatus getAccountStatus()  { return accountStatus; }
    public double getAnnualVolume()          { return annualVolume; }

    public void setAccountStatus(AccountStatus status) { this.accountStatus = status; }
    public void setDepartmentId(int departmentId)       { this.departmentId = departmentId; }
    public void setAnnualVolume(double annualVolume)    { this.annualVolume = annualVolume; }
}
