package medisync.model;

// [Practical 3] Model class for Appointment entity
// [Practical 5] Packages: medisync.model
public class Appointment {
    private int id;
    private String patientId;
    private int doctorId;
    private String slotTime;
    private String status;

    // [Practical 3] Default constructor
    public Appointment() {
        this(0, "", 0, "", "BOOKED");
    }

    // [Practical 3] Parameterized constructor
    public Appointment(int id, String patientId, int doctorId, String slotTime, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotTime = slotTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
