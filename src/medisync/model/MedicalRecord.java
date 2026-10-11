package medisync.model;

// [Practical 3] Model class for MedicalRecord entity
// [Practical 5] Packages: medisync.model
public class MedicalRecord {
    private int id;
    private String patientId;
    private String diagnosis;
    private String prescription;
    private String doctorName;
    private String dateText;

    // [Practical 3] Default constructor
    public MedicalRecord() {
        this(0, "", "", "", "", "");
    }

    // [Practical 3] Parameterized constructor
    public MedicalRecord(int id, String patientId, String diagnosis, String prescription, String doctorName, String dateText) {
        this.id = id;
        this.patientId = patientId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.doctorName = doctorName;
        this.dateText = dateText;
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

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDateText() {
        return dateText;
    }
}
