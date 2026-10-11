package grievance.model;

// [Practical 4] Abstract class for municipal departments
// [Practical 5] Packages: grievance.model
public abstract class Department {
    protected String deptName;

    // [Practical 6] Constructor called by super(name) in subclasses
    public Department(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptName() {
        return deptName;
    }

    // [Practical 4] Abstract method overridden by each department subclass
    public abstract String handle(Complaint c);
}
