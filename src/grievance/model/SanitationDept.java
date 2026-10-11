package grievance.model;

// [Practical 4] Subclass overriding handle()
// [Practical 5] Packages: grievance.model
public class SanitationDept extends Department {

    public SanitationDept() {
        // [Practical 6] super keyword
        super("Solid Waste & Sanitation Department");
    }

    // [Practical 4] Method overriding
    @Override
    public String handle(Complaint c) {
        return "Sanitation worker gang and waste disposal truck assigned for clearing site for Ticket #" + c.getId();
    }
}
