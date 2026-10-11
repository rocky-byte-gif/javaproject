package medisync.service;

import java.util.Vector;
import medisync.db.ClinicDao;
import medisync.exception.InvalidPatientException;
import medisync.exception.PatientNotFoundException;
import medisync.exception.SlotNotAvailableException;
import medisync.model.Appointment;
import medisync.model.Bill;
import medisync.model.Doctor;
import medisync.model.MedicalRecord;
import medisync.model.Patient;

// [Practical 4] Interface implementation
// [Practical 5] Packages: medisync.service
public class ClinicService implements Reportable {

    // [Practical 2] Vector collections for in-memory clinic state
    private Vector<Patient> patients;
    private Vector<Doctor> doctors;
    private Vector<Appointment> appointments;
    private Vector<Patient> waitingList;
    private ClinicDao dao;

    public ClinicService(ClinicDao dao) {
        this.dao = dao;
        this.patients = new Vector<Patient>();
        this.doctors = new Vector<Doctor>();
        this.appointments = new Vector<Appointment>();
        this.waitingList = new Vector<Patient>();
    }

    public ClinicDao getDao() {
        return dao;
    }

    public Vector<Patient> getPatients() {
        return patients;
    }

    public Vector<Doctor> getDoctors() {
        return doctors;
    }

    public Vector<Appointment> getAppointments() {
        return appointments;
    }

    public Vector<Patient> getWaitingList() {
        return waitingList;
    }

    // [Practical 4] Overloaded method 1: addPatient
    public Patient addPatient(String name, int age, String phone) throws InvalidPatientException {
        return addPatient(name, age, phone, "N/A", "None");
    }

    // [Practical 4] Overloaded method 2: addPatient with blood group and allergies
    public Patient addPatient(String name, int age, String phone, String bloodGroup, String allergies) throws InvalidPatientException {
        // [Practical 9] String method: trim and length
        if (name == null || name.trim().length() == 0) {
            throw new InvalidPatientException("Patient name cannot be empty.");
        }
        if (age < 0 || age > 120) {
            throw new InvalidPatientException("Patient age must be between 0 and 120.");
        }
        if (phone == null || phone.trim().length() != 10) {
            throw new InvalidPatientException("Phone number must be exactly 10 digits.");
        }
        String cleanPhone = phone.trim();
        for (int i = 0; i < cleanPhone.length(); i++) {
            char c = cleanPhone.charAt(i);
            if (c < '0' || c > '9') {
                throw new InvalidPatientException("Phone number must contain only numeric digits.");
            }
        }

        Patient p = new Patient(name, age, cleanPhone, bloodGroup, allergies);

        // [Practical 8] Synchronized access to patients Vector
        synchronized (patients) {
            patients.addElement(p);
        }
        if (dao != null) {
            dao.savePatient(p);
        }
        return p;
    }

    // Find patient by ID
    public Patient findPatient(String id) throws PatientNotFoundException {
        if (id == null) {
            throw new PatientNotFoundException("Patient ID cannot be null.");
        }
        synchronized (patients) {
            for (int i = 0; i < patients.size(); i++) {
                Patient p = patients.elementAt(i);
                // [Practical 9] String method: equalsIgnoreCase
                if (p.getId().equalsIgnoreCase(id.trim())) {
                    return p;
                }
            }
        }
        // [Practical 7] Throw custom exception
        throw new PatientNotFoundException("Patient with ID " + id + " was not found.");
    }

    // Find doctor by ID
    public Doctor findDoctor(int id) {
        synchronized (doctors) {
            for (int i = 0; i < doctors.size(); i++) {
                Doctor d = doctors.elementAt(i);
                if (d.getId() == id) {
                    return d;
                }
            }
        }
        return null;
    }

    // [Practical 9] Search patients using toLowerCase and contains
    public Vector<Patient> searchPatients(String text) {
        Vector<Patient> matches = new Vector<Patient>();
        if (text == null || text.trim().length() == 0) {
            return patients;
        }
        String query = text.trim().toLowerCase();
        synchronized (patients) {
            for (int i = 0; i < patients.size(); i++) {
                Patient p = patients.elementAt(i);
                if (p.getName().toLowerCase().contains(query) || p.getId().toLowerCase().contains(query)) {
                    matches.addElement(p);
                }
            }
        }
        return matches;
    }

    // Remove patient by ID
    public boolean removePatient(String id) {
        synchronized (patients) {
            for (int i = 0; i < patients.size(); i++) {
                if (patients.elementAt(i).getId().equalsIgnoreCase(id)) {
                    patients.removeElementAt(i);
                    if (dao != null) {
                        dao.deletePatient(id);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // [Practical 4] Overloaded method 1: bookAppointment
    public Appointment bookAppointment(String patientId, int doctorId) throws PatientNotFoundException, SlotNotAvailableException {
        return bookAppointment(patientId, doctorId, false);
    }

    // [Practical 4] Overloaded method 2: bookAppointment with emergency flag
    public Appointment bookAppointment(String patientId, int doctorId, boolean emergency) throws PatientNotFoundException, SlotNotAvailableException {
        Patient p = findPatient(patientId);
        Doctor d = findDoctor(doctorId);
        if (d == null) {
            throw new SlotNotAvailableException("Doctor #" + doctorId + " does not exist.");
        }

        Appointment appt;
        try {
            // [Practical 8] Doctor's synchronized method ensures no slot is double-booked
            appt = d.bookSlot(p);
            synchronized (appointments) {
                appointments.addElement(appt);
            }
            if (dao != null) {
                dao.saveAppointment(appt);
            }
        } catch (SlotNotAvailableException e) {
            // If doctor is full, add patient to waiting list
            addToWaitingList(p, emergency);
            throw new SlotNotAvailableException("Doctor full. Patient added to waiting list.");
        }
        return appt;
    }

    // Cancel appointment
    public boolean cancelAppointment(int appointmentId) {
        synchronized (appointments) {
            for (int i = 0; i < appointments.size(); i++) {
                Appointment a = appointments.elementAt(i);
                if (a.getId() == appointmentId) {
                    a.setStatus("CANCELLED");
                    Doctor d = findDoctor(a.getDoctorId());
                    if (d != null) {
                        d.freeSlotByTime(a.getSlotTime());
                    }
                    if (dao != null) {
                        dao.updateAppointmentStatus(appointmentId, "CANCELLED");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // [Practical 2] Add patient to waiting list: emergency patients inserted at front
    public void addToWaitingList(Patient p, boolean emergency) {
        synchronized (waitingList) {
            // Avoid duplicate waiting entries
            for (int i = 0; i < waitingList.size(); i++) {
                if (waitingList.elementAt(i).getId().equalsIgnoreCase(p.getId())) {
                    return;
                }
            }
            if (emergency) {
                // [Practical 2] Vector insertElementAt(p, 0)
                waitingList.insertElementAt(p, 0);
                System.out.println("[ClinicService] Emergency Patient " + p.getId() + " placed at front of waitlist.");
            } else {
                waitingList.addElement(p);
                System.out.println("[ClinicService] Patient " + p.getId() + " added to waitlist.");
            }
        }
    }

    // Add bill
    public void addBill(Bill b) {
        if (dao != null) {
            dao.saveBill(b);
        }
    }

    // Update bill status
    public void updateBillStatus(int id, String status) {
        if (dao != null) {
            dao.updateBillStatus(id, status);
        }
    }

    // Add medical record
    public void addRecord(MedicalRecord r) {
        if (dao != null) {
            dao.saveRecord(r);
        }
    }

    // [Practical 4] Overriding getSummary from Reportable interface
    @Override
    public String getSummary() {
        int patCount = patients.size();
        int docCount = doctors.size();
        int apptCount = appointments.size();
        int waitCount = waitingList.size();

        // [Practical 2] Array: int[3] bill status counts [0: PENDING, 1: PAID, 2: CANCELLED]
        int[] billCounts = new int[3];
        Vector<Bill> bills = dao != null ? dao.getBills() : new Vector<Bill>();
        for (int i = 0; i < bills.size(); i++) {
            String st = bills.elementAt(i).getStatus();
            // [Practical 1] Switch on status string
            if ("PAID".equalsIgnoreCase(st)) {
                billCounts[1]++;
            } else if ("CANCELLED".equalsIgnoreCase(st)) {
                billCounts[2]++;
            } else {
                billCounts[0]++;
            }
        }

        return "Patients: " + patCount + " | Doctors: " + docCount + " | Appointments: " + apptCount +
               " | Waiting List: " + waitCount + " | Bills: [Pending: " + billCounts[0] + ", Paid: " + billCounts[1] + ", Cancelled: " + billCounts[2] + "]";
    }
}
