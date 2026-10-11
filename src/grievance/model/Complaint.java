package grievance.model;

// [Practical 5] Packages: model package for complaint entity
public class Complaint {
    // [Practical 4] Encapsulation: private fields
    private int id;
    private String name;
    private String text;
    private String ward;
    private String category;
    private int priority;
    private String status;
    private long createdAt;

    // [Practical 6] static counter for generating auto IDs
    public static int counter = 100;

    // [Practical 3] Classes and constructors: default constructor
    // [Practical 6] this() constructor chaining
    public Complaint() {
        this(0, "Anonymous", "No complaint details provided", "Ward 1", "GENERAL", 1, "OPEN");
    }

    // [Practical 3] Parameterized constructor
    public Complaint(int id, String name, String text, String ward, String category, int priority, String status) {
        this.id = id;
        this.name = formatName(name);
        this.text = text != null ? text.trim() : "";
        this.ward = ward != null ? ward : "Ward 1";
        this.category = category != null ? category : "GENERAL";
        this.priority = priority;
        this.status = status != null ? status : "OPEN";
        this.createdAt = System.currentTimeMillis();
    }

    // [Practical 9] String methods: trim, toUpperCase, charAt, substring, concat
    private String formatName(String n) {
        if (n == null) {
            return "Anonymous";
        }
        String clean = n.trim();
        if (clean.length() == 0) {
            return "Anonymous";
        }
        // Capitalize first character and concatenate rest of string
        char firstChar = Character.toUpperCase(clean.charAt(0));
        if (clean.length() == 1) {
            return String.valueOf(firstChar);
        }
        String rest = clean.substring(1);
        return String.valueOf(firstChar).concat(rest);
    }

    // [Practical 9] String method: substring (first 40 chars preview)
    public String getPreview() {
        if (text == null) {
            return "";
        }
        if (text.length() <= 40) {
            return text;
        }
        return text.substring(0, 40).concat("...");
    }

    // [Practical 4] Encapsulation: public getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = formatName(name);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
