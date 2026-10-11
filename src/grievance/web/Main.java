package grievance.web;

import java.util.Vector;
import grievance.db.ComplaintDao;
import grievance.model.Complaint;
import grievance.service.ComplaintEngine;
import grievance.service.SlaMonitor;

// [Practical 5] Packages: grievance.web
public class Main {

    // [Practical 1] Main method: entry point of Java program
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  GrievanceGPT - Municipal Complaint System");
        System.out.println("  B.Tech CSE III Year Java Lab Project");
        System.out.println("==================================================");

        // Instantiate DAO and ComplaintEngine
        ComplaintDao dao = new ComplaintDao();
        ComplaintEngine engine = new ComplaintEngine();

        // [Practical 6] Initialize static counter from MAX(id) in the database
        int maxId = dao.getMaxId();
        Complaint.counter = maxId;
        System.out.println("[Main] Initialized Ticket Counter to: " + Complaint.counter);

        // Load existing complaints from DB or seed initial sample records into queue
        Vector<Complaint> existing = dao.findByCategory("ALL");
        if (existing.size() > 0) {
            for (int i = 0; i < existing.size(); i++) {
                engine.getQueue().addElement(existing.elementAt(i));
            }
            System.out.println("[Main] Loaded " + existing.size() + " records from MySQL database.");
        } else {
            // Seed 3 sample records into queue for in-memory testing if MySQL is not configured
            System.out.println("[Main] MySQL empty or offline; seeding 3 sample complaints into queue.");
            Complaint c1 = new Complaint(101, "Ramesh Kumar", "Pipe leakage on main market road since two days", "Ward 12", "WATER", 6, "OPEN");
            Complaint c2 = new Complaint(102, "Priya Sharma", "Huge pothole causing traffic jam near bus stand", "Ward 5", "ROAD", 8, "ESCALATED");
            Complaint c3 = new Complaint(103, "Amit Verma", "Garbage bin overflowing near primary school", "Ward 8", "SANITATION", 5, "RESOLVED");

            engine.getQueue().addElement(c1);
            engine.getQueue().addElement(c2);
            engine.getQueue().addElement(c3);
        }

        // [Practical 8] Start background SlaMonitor thread
        SlaMonitor slaThread = new SlaMonitor(engine.getQueue(), dao);
        slaThread.setDaemon(true);
        slaThread.start();
        System.out.println("[Main] SlaMonitor background thread started (SLA = " + SlaMonitor.SLA_SECONDS + "s).");

        // Start WebServer on port 8080
        int port = 8080;
        try {
            WebServer webServer = new WebServer(port, engine, dao);
            webServer.start();
            System.out.println("[Main] Application ready! Open: http://localhost:" + port + "/");
        } catch (Exception e) {
            System.out.println("[Main] Failed to start WebServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
