package grievance.service;

import grievance.db.ComplaintDao;
import grievance.model.Complaint;

// [Practical 5] Packages: grievance.service
// [Practical 8] Multithreading: implements Runnable interface
public class ResolverTask implements Runnable {

    private Complaint complaint;
    private ComplaintDao dao;
    private int delaySeconds;

    public ResolverTask(Complaint complaint, ComplaintDao dao, int delaySeconds) {
        this.complaint = complaint;
        this.dao = dao;
        this.delaySeconds = delaySeconds;
    }

    // [Practical 8] Runnable execution logic
    @Override
    public void run() {
        try {
            // Sleep for demo duration to simulate field work resolution
            Thread.sleep(delaySeconds * 1000L);

            complaint.setStatus("RESOLVED");
            System.out.println("[Resolver Task] Resolved Ticket #" + complaint.getId());

            if (dao != null) {
                dao.updateStatus(complaint.getId(), "RESOLVED");
            }
        } catch (InterruptedException e) {
            System.out.println("[Resolver Task] Interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[Resolver Task] Error: " + e.getMessage());
        }
    }
}
