package org.example.model;

/**
 * Represents a customer registered with the fashion house's Customer Relations department.
 *
 * CSV format (7 fields, split(",", 7)):
 *   id,fullName,email,phone,preferredContact,tier,registrationDate
 *
 * Profiles are immutable after registration — no setters.
 */
public class CustomerProfile {

    public enum PreferredContact { EMAIL, PHONE, EITHER }
    public enum Tier { STANDARD, PREMIUM, VIP }

    private final int id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final PreferredContact preferredContact;
    private final Tier tier;
    private final String registrationDate;

    public CustomerProfile(int id, String fullName, String email, String phone,
                           PreferredContact preferredContact, Tier tier,
                           String registrationDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.preferredContact = preferredContact;
        this.tier = tier;
        this.registrationDate = registrationDate;
    }

    public String toCSV() {
        return id + "," + fullName + "," + email + "," + phone + ","
             + preferredContact.name() + "," + tier.name() + "," + registrationDate;
    }

    public static CustomerProfile fromCSV(String line) {
        String[] p = line.split(",", 7);
        return new CustomerProfile(
            Integer.parseInt(p[0].trim()),
            p[1].trim(),
            p[2].trim(),
            p[3].trim(),
            PreferredContact.valueOf(p[4].trim()),
            Tier.valueOf(p[5].trim()),
            p[6].trim()
        );
    }

    @Override
    public String toString() {
        return "[" + id + "] " + fullName
             + " | Email: " + email
             + " | Phone: " + phone
             + " | Contact: " + preferredContact
             + " | Tier: " + tier
             + " | Registered: " + registrationDate;
    }

    public int getId()                        { return id; }
    public String getFullName()               { return fullName; }
    public String getEmail()                  { return email; }
    public String getPhone()                  { return phone; }
    public PreferredContact getPreferredContact() { return preferredContact; }
    public Tier getTier()                     { return tier; }
    public String getRegistrationDate()       { return registrationDate; }
}
