package grievance.model;

// [Practical 4] Subclass overriding handle()
// [Practical 5] Packages: grievance.model
public class ElectricityDept extends Department {

    public ElectricityDept() {
        // [Practical 6] super keyword
        super("Electricity Board & Street Lighting Department");
    }

    // [Practical 4] Method overriding
    @Override
    public String handle(Complaint c) {
        return "Electrician line-men team dispatched to test transformer and repair power line for Ticket #" + c.getId();
    }
}
