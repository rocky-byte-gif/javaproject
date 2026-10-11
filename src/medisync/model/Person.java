package medisync.model;

// [Practical 4] Abstract base class
// [Practical 5] Packages: medisync.model
public abstract class Person {

    // [Practical 4] Encapsulation: private fields
    private String name;
    private int age;
    private String phone;

    // [Practical 3] Default constructor
    // [Practical 6] this() constructor chaining
    public Person() {
        this("Unknown", 0, "0000000000");
    }

    // [Practical 3] Parameterized constructor
    public Person(String name, int age, String phone) {
        setName(name);
        setAge(age);
        setPhone(phone);
    }

    // [Practical 4] Validated setters
    public void setName(String name) {
        // [Practical 9] String method: trim and length
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name.trim();
    }

    public void setAge(int age) {
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }
        this.age = age;
    }

    public void setPhone(String phone) {
        // [Practical 9] String method: length and charAt loop check
        if (phone == null || phone.trim().length() != 10) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits");
        }
        String clean = phone.trim();
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Phone must contain only numeric digits");
            }
        }
        this.phone = clean;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    // [Practical 4] Abstract method to be overridden by Patient and Doctor
    public abstract String getRole();

    // [Practical 6] final method: proper-case name formatting
    // [Practical 9] String methods: trim, toUpperCase, charAt, substring, toLowerCase, concat
    public final String getDisplayName() {
        if (name == null || name.length() == 0) {
            return "Unknown";
        }
        String[] words = name.trim().split(" ");
        String result = "";
        for (int i = 0; i < words.length; i++) {
            String w = words[i].trim();
            if (w.length() > 0) {
                char first = Character.toUpperCase(w.charAt(0));
                String rest = w.length() > 1 ? w.substring(1).toLowerCase() : "";
                String proper = String.valueOf(first).concat(rest);
                if (result.length() > 0) {
                    result = result.concat(" ").concat(proper);
                } else {
                    result = proper;
                }
            }
        }
        return result;
    }
}
