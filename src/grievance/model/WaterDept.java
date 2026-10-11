package grievance.model;

// [Practical 4] Method overriding and subclassing
// [Practical 5] Packages: grievance.model
public class WaterDept extends Department {

    public WaterDept() {
        // [Practical 6] Calling parent constructor using super()
        super("Water Supply & Drainage Department");
    }

    // [Practical 4] Overriding handle() method
    @Override
    public String handle(Complaint c) {
        return "Dispatched plumbing maintenance team and water pipeline inspection van for Ticket #" + c.getId();
    }
}
