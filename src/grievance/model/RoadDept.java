package grievance.model;

// [Practical 4] Subclass overriding handle()
// [Practical 5] Packages: grievance.model
public class RoadDept extends Department {

    public RoadDept() {
        // [Practical 6] super keyword
        super("Roads & Traffic Infrastructure Department");
    }

    // [Practical 4] Method overriding
    @Override
    public String handle(Complaint c) {
        return "Notified highway engineers and road repair crew to inspect and patch site for Ticket #" + c.getId();
    }
}
