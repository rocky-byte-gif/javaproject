package grievance.model;

// [Practical 4] Subclass overriding handle()
// [Practical 5] Packages: grievance.model
public class GeneralDept extends Department {

    public GeneralDept() {
        // [Practical 6] super keyword
        super("General Municipal Administration & Helpdesk");
    }

    // [Practical 4] Method overriding
    @Override
    public String handle(Complaint c) {
        return "Complaint logged into municipal general registry; officer assigned for verification for Ticket #" + c.getId();
    }
}
