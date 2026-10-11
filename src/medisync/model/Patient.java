package medisync.model;

// [Practical 4] Subclass inheritance
// [Practical 5] Packages: medisync.model
public class Patient extends Person {

    // [Practical 6] static counter for auto-generating patient IDs
    public static int counter = 0;

    private String id;
    private String bloodGroup;
    private String allergies;

    // [Practical 4] Overloaded constructor 1
    // [Practical 6] this() chaining
    public Patient(String name, int age, String phone) {
        this(name, age, phone, "N/A", "None");
    }

    // [Practical 4] Overloaded constructor 2
    // [Practical 6] super() call to parent Person constructor
    public Patient(String name, int age, String phone, String bloodGroup, String allergies) {
        super(name, age, phone);
        this.id = generateNextId();
        this.bloodGroup = bloodGroup != null && bloodGroup.trim().length() > 0 ? bloodGroup.trim() : "N/A";
        this.allergies = allergies != null && allergies.trim().length() > 0 ? allergies.trim() : "None";
    }

    // Constructor for loading from database
    public Patient(String id, String name, int age, String phone, String bloodGroup, String allergies) {
        super(name, age, phone);
        this.id = id;
        this.bloodGroup = bloodGroup != null ? bloodGroup : "N/A";
        this.allergies = allergies != null ? allergies : "None";
    }

    // [Practical 6] static helper for patient ID format MS-2026-0042
    // [Practical 9] String method: length and concat
    public static synchronized String generateNextId() {
        counter++;
        String numStr = String.valueOf(counter);
        while (numStr.length() < 4) {
            numStr = "0".concat(numStr);
        }
        return "MS-2026-".concat(numStr);
    }

    // [Practical 4] Overriding abstract getRole method
    @Override
    public String getRole() {
        return "Patient";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
}
