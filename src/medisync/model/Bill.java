package medisync.model;

import medisync.exception.InvalidBillException;
import medisync.service.Billable;

// [Practical 4] Interface implementation
// [Practical 5] Packages: medisync.model
public class Bill implements Billable {

    // [Practical 6] static counter for bill numbers
    public static int counter = 0;

    private int id;
    private String patientId;
    private double consultation;
    private double medicine;
    private double testFee;
    private double total;
    private String status; // PENDING, PAID, CANCELLED

    // Constructor taking the Builder
    public Bill(Builder b) {
        counter++;
        this.id = counter;
        this.patientId = b.bPatientId;
        this.consultation = b.bConsultation;
        this.medicine = b.bMedicine;
        this.testFee = b.bTestFee;
        this.total = calculateTotal();
        this.status = "PENDING";
    }

    // Constructor for loading from database
    public Bill(int id, String patientId, double consultation, double medicine, double testFee, double total, String status) {
        this.id = id;
        this.patientId = patientId;
        this.consultation = consultation;
        this.medicine = medicine;
        this.testFee = testFee;
        this.total = total;
        this.status = status != null ? status : "PENDING";
    }

    // [Practical 4] Overriding calculateTotal from Billable interface
    // [Practical 1] Type casting or double addition
    @Override
    public double calculateTotal() {
        return consultation + medicine + testFee;
    }

    // [Practical 6] Nested static Builder class with method chaining (returning this)
    public static class Builder {
        private String bPatientId = "";
        private double bConsultation = 0.0;
        private double bMedicine = 0.0;
        private double bTestFee = 0.0;

        public Builder patientId(String pid) {
            this.bPatientId = pid;
            return this;
        }

        public Builder consultation(double fee) {
            this.bConsultation = fee;
            return this;
        }

        public Builder medicine(double fee) {
            this.bMedicine = fee;
            return this;
        }

        public Builder testFee(double fee) {
            this.bTestFee = fee;
            return this;
        }

        // [Practical 7] Throws custom InvalidBillException if fee negative or all fees zero
        public Bill build() throws InvalidBillException {
            if (bConsultation < 0 || bMedicine < 0 || bTestFee < 0) {
                throw new InvalidBillException("Bill fees cannot be negative.");
            }
            if (bConsultation == 0 && bMedicine == 0 && bTestFee == 0) {
                throw new InvalidBillException("At least one bill fee must be greater than zero.");
            }
            if (bPatientId == null || bPatientId.trim().length() == 0) {
                throw new InvalidBillException("Patient ID is required to generate bill.");
            }
            return new Bill(this);
        }
    }

    public int getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public double getConsultation() {
        return consultation;
    }

    public double getMedicine() {
        return medicine;
    }

    public double getTestFee() {
        return testFee;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
