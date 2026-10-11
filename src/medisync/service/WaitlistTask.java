package medisync.service;

import java.util.Vector;
import medisync.model.Appointment;
import medisync.model.Doctor;
import medisync.model.Patient;

// [Practical 5] Packages: medisync.service
// [Practical 8] Multithreading: implements Runnable for background daemon task
public class WaitlistTask implements Runnable {

    private ClinicService service;
    private boolean active;

    public WaitlistTask(ClinicService service) {
        this.service = service;
        this.active = true;
    }

    public void stopTask() {
        this.active = false;
    }

    // [Practical 8] Background execution logic
    @Override
    public void run() {
        while (active) {
            try {
                // Check waitlist every 3 seconds
                Thread.sleep(3000);

                Vector<Patient> waitlist = service.getWaitingList();
                Vector<Doctor> doctors = service.getDoctors();

                // [Practical 8] Synchronized access to waitlist
                synchronized (waitlist) {
                    if (!waitlist.isEmpty()) {
                        for (int i = 0; i < doctors.size(); i++) {
                            Doctor d = doctors.elementAt(i);
                            if (d.getFreeCount() > 0 && !waitlist.isEmpty()) {
                                Patient p = waitlist.remove(0);
                                try {
                                    Appointment a = d.bookSlot(p);
                                    if (service.getDao() != null) {
                                        service.getDao().saveAppointment(a);
                                    }
                                    synchronized (service.getAppointments()) {
                                        service.getAppointments().addElement(a);
                                    }
                                    System.out.println("[WaitlistTask] Auto-booked waiting patient " + p.getId() + " with " + d.getName() + " at " + a.getSlotTime());
                                } catch (Exception e) {
                                    System.out.println("[WaitlistTask] Booking error: " + e.getMessage());
                                }
                            }
                        }
                    }
                }

            } catch (InterruptedException e) {
                System.out.println("[WaitlistTask] Interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[WaitlistTask] Error: " + e.getMessage());
            }
        }
    }
}
