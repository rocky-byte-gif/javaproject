package medisync.service;

import java.util.Vector;
import medisync.exception.SlotNotAvailableException;
import medisync.model.Appointment;
import medisync.model.Doctor;
import medisync.model.Patient;

// [Practical 5] Packages: medisync.service
// [Practical 8] Multithreading: extends Thread
public class BookingThread extends Thread {

    private Patient patient;
    private int doctorId;
    private ClinicService service;
    private Vector<String> resultLog;

    public BookingThread(Patient patient, int doctorId, ClinicService service, Vector<String> resultLog) {
        this.patient = patient;
        this.doctorId = doctorId;
        this.service = service;
        this.resultLog = resultLog;
    }

    // [Practical 8] Thread run method simulating concurrent booking attempt
    @Override
    public void run() {
        try {
            // [Practical 8] Thread.sleep: random delay between 0 and 200 ms
            int delay = (int) (Math.random() * 200);
            Thread.sleep(delay);

            Appointment appt = service.bookAppointment(patient.getId(), doctorId);

            Doctor doc = service.findDoctor(doctorId);
            String docName = doc != null ? doc.getName() : ("Doctor #" + doctorId);
            String line = patient.getId() + " -> " + docName + " at " + appt.getSlotTime() + " BOOKED (thread delay: " + delay + "ms)";

            // [Practical 8] Synchronized block on shared result list
            synchronized (resultLog) {
                resultLog.addElement(line);
            }

        } catch (SlotNotAvailableException e) {
            String line = patient.getId() + " -> Doctor #" + doctorId + " FULL -> MOVED TO WAITING LIST";
            synchronized (resultLog) {
                resultLog.addElement(line);
            }
        } catch (Exception e) {
            String line = patient.getId() + " -> ERROR: " + e.getMessage();
            synchronized (resultLog) {
                resultLog.addElement(line);
            }
        }
    }
}
