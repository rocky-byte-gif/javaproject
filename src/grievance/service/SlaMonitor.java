package grievance.service;

import java.util.Vector;
import grievance.db.ComplaintDao;
import grievance.model.Complaint;

// [Practical 5] Packages: grievance.service
// [Practical 8] Multithreading: extends Thread class
public class SlaMonitor extends Thread {

    // [Practical 6] final constant: SLA threshold in seconds (120s for demo)
    public static final int SLA_SECONDS = 120;

    private Vector<Complaint> queue;
    private ComplaintDao dao;
    private boolean active;

    public SlaMonitor(Vector<Complaint> queue, ComplaintDao dao) {
        this.queue = queue;
        this.dao = dao;
        this.active = true;
    }

    public void stopMonitor() {
        this.active = false;
    }

    // [Practical 8] Thread entry point run() method
    @Override
    public void run() {
        while (active) {
            try {
                // Sleep for 5 seconds between checks
                Thread.sleep(5000);

                long now = System.currentTimeMillis();

                // [Practical 8] Synchronized block on Vector queue
                synchronized (queue) {
                    for (int i = 0; i < queue.size(); i++) {
                        Complaint c = queue.elementAt(i);

                        // [Practical 9] String method: equalsIgnoreCase
                        if ("OPEN".equalsIgnoreCase(c.getStatus())) {
                            long elapsedSeconds = (now - c.getCreatedAt()) / 1000;

                            // If elapsed time exceeds SLA, mark as ESCALATED
                            if (elapsedSeconds >= SLA_SECONDS) {
                                c.setStatus("ESCALATED");
                                System.out.println("[SLA Monitor] Escalated Ticket #" + c.getId() + " after " + elapsedSeconds + "s");

                                // Update DB record if dao is connected
                                if (dao != null) {
                                    dao.updateStatus(c.getId(), "ESCALATED");
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("[SLA Monitor] Thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[SLA Monitor] Error: " + e.getMessage());
            }
        }
    }
}
