package coldtrack.web;

import java.util.Vector;
import coldtrack.db.ColdTrackDao;
import coldtrack.model.Alert;
import coldtrack.model.FoodTruck;
import coldtrack.model.FrozenTruck;
import coldtrack.model.Truck;
import coldtrack.model.VaccineTruck;
import coldtrack.service.ColdChainService;
import coldtrack.service.ProcessorTask;
import coldtrack.service.SensorThread;

// [Practical 5] Packages: coldtrack.web
public class Main {

    // [Practical 1] Main method: entry point of Java program
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  ColdTrack - Cold Chain Temperature Monitor");
        System.out.println("  B.Tech CSE III Year Java Lab Mini Project");
        System.out.println("==================================================");

        // [Practical 10] Instantiate DAO
        ColdTrackDao dao = new ColdTrackDao();

        // [Practical 6] Initialize Alert static counter from DB
        int maxAlertId = dao.getMaxAlertId();
        Alert.counter = maxAlertId;
        System.out.println("[Main] Initialized Alert.counter to: " + Alert.counter);

        // Instantiate ColdChainService
        ColdChainService service = new ColdChainService(dao);

        // [Practical 10] Load existing trucks from DB or insert 3 defaults
        Vector<Truck> existing = dao.loadTrucks();
        if (existing.size() > 0) {
            for (int i = 0; i < existing.size(); i++) {
                service.getTrucks().addElement(existing.elementAt(i));
            }
            System.out.println("[Main] Loaded " + existing.size() + " trucks from MySQL database.");
        } else {
            System.out.println("[Main] Database empty or offline; inserting 3 default trucks.");
            Truck t1 = new VaccineTruck(1, "Truck A", 2.0, 8.0);
            Truck t2 = new FrozenTruck(2, "Truck B", -20.0, -15.0);
            Truck t3 = new FoodTruck(3, "Truck C", 0.0, 5.0);

            service.getTrucks().addElement(t1);
            service.getTrucks().addElement(t2);
            service.getTrucks().addElement(t3);

            dao.saveTruck(t1);
            dao.saveTruck(t2);
            dao.saveTruck(t3);
        }

        // [Practical 8] Start one SensorThread per truck
        Vector<Truck> currentTrucks = service.getTrucks();
        for (int i = 0; i < currentTrucks.size(); i++) {
            Truck t = currentTrucks.elementAt(i);
            SensorThread st = new SensorThread(t, service.getInbox());
            st.setDaemon(true);
            st.start();
            System.out.println("[Main] Started SensorThread for " + t.getName() + " (ID #" + t.getId() + ")");
        }

        // [Practical 8] Start ProcessorTask worker thread
        ProcessorTask processor = new ProcessorTask(service, service.getInbox());
        Thread procThread = new Thread(processor);
        procThread.setDaemon(true);
        procThread.start();
        System.out.println("[Main] Started ProcessorTask thread consuming sensor inbox.");

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
