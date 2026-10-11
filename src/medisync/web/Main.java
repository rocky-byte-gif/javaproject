package medisync.web;

import java.util.Vector;
import medisync.db.ClinicDao;
import medisync.model.Appointment;
import medisync.model.Bill;
import medisync.model.Doctor;
import medisync.model.Patient;
import medisync.service.ClinicService;
import medisync.service.WaitlistTask;

// [Practical 5] Packages: medisync.web
public class Main {

    // [Practical 1] Main method: program execution entry point
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  MediSync Lite - Clinic Management System");
        System.out.println("  B.Tech CSE III Year Java Lab Mini Project");
        System.out.println("==================================================");

        // [Practical 10] Instantiate DAO
        ClinicDao dao = new ClinicDao();

        // [Practical 6] Initialize static counters from database MAX values
        int maxPat = dao.getMaxPatientNumber();
        Patient.counter = maxPat;
        System.out.println("[Main] Initialized Patient.counter to: " + Patient.counter);

        int maxBill = dao.getMaxBillId();
        Bill.counter = maxBill;
        System.out.println("[Main] Initialized Bill.counter to: " + Bill.counter);

        // Instantiate ClinicService
        ClinicService service = new ClinicService(dao);

        // [Practical 10] Load doctors or insert 3 defaults
        Vector<Doctor> docs = dao.loadDoctors();
        if (docs.size() > 0) {
            for (int i = 0; i < docs.size(); i++) {
                service.getDoctors().addElement(docs.elementAt(i));
            }
            System.out.println("[Main] Loaded " + docs.size() + " doctors from database.");
        } else {
            System.out.println("[Main] Inserting 3 default clinic doctors.");
            Doctor d1 = new Doctor(1, "Dr. Mehta", "General");
            Doctor d2 = new Doctor(2, "Dr. Rao", "Cardiology");
            Doctor d3 = new Doctor(3, "Dr. Khan", "Pediatrics");

            service.getDoctors().addElement(d1);
            service.getDoctors().addElement(d2);
            service.getDoctors().addElement(d3);
        }

        // [Practical 10] Load patients or insert 3 defaults
        Vector<Patient> pats = dao.loadPatients();
        if (pats.size() > 0) {
            for (int i = 0; i < pats.size(); i++) {
                service.getPatients().addElement(pats.elementAt(i));
            }
            System.out.println("[Main] Loaded " + pats.size() + " patients from database.");
        } else {
            System.out.println("[Main] Inserting 3 default demo patients.");
            try {
                Patient p1 = new Patient("MS-2026-0001", "Aarav Sharma", 32, "9876543210", "B+", "None");
                Patient p2 = new Patient("MS-2026-0002", "Sunita Deshmukh", 45, "9822334455", "O+", "Penicillin");
                Patient p3 = new Patient("MS-2026-0003", "Vikram Joshi", 28, "9123456789", "A+", "Dust");

                service.getPatients().addElement(p1);
                service.getPatients().addElement(p2);
                service.getPatients().addElement(p3);

                dao.savePatient(p1);
                dao.savePatient(p2);
                dao.savePatient(p3);
            } catch (Exception e) {
                System.out.println("[Main] Seed error: " + e.getMessage());
            }
        }

        // [Practical 10] Load appointments and sync doctor slot states
        Vector<Appointment> appts = dao.loadAppointments();
        for (int i = 0; i < appts.size(); i++) {
            Appointment a = appts.elementAt(i);
            service.getAppointments().addElement(a);
            if ("BOOKED".equalsIgnoreCase(a.getStatus())) {
                Doctor d = service.findDoctor(a.getDoctorId());
                if (d != null) {
                    d.markSlotBookedByTime(a.getSlotTime());
                }
            }
        }
        System.out.println("[Main] Loaded " + appts.size() + " appointments and synchronized doctor schedules.");

        // [Practical 8] Start WaitlistTask in a daemon thread
        WaitlistTask waitlistTask = new WaitlistTask(service);
        Thread waitlistThread = new Thread(waitlistTask);
        waitlistThread.setDaemon(true);
        waitlistThread.start();
        System.out.println("[Main] Started WaitlistTask background daemon thread.");

        // Start WebServer on port 8080
        int port = 8080;
        try {
            WebServer webServer = new WebServer(port, service, dao);
            webServer.start();
            System.out.println("[Main] Server running successfully! Open http://localhost:" + port);
        } catch (Exception e) {
            System.out.println("[Main] Failed to start WebServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
