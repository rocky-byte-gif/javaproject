package coldtrack.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import coldtrack.db.ColdTrackDao;
import coldtrack.exception.InvalidReadingException;
import coldtrack.exception.TemperatureBreachException;
import coldtrack.exception.TruckNotFoundException;
import coldtrack.model.Alert;
import coldtrack.model.FoodTruck;
import coldtrack.model.FrozenTruck;
import coldtrack.model.Reading;
import coldtrack.model.Truck;
import coldtrack.model.VaccineTruck;

// [Practical 4] Multiple inheritance via interfaces (implements Alertable and Reportable)
// [Practical 5] Packages: coldtrack.service
public class ColdChainService implements Alertable, Reportable {

    // [Practical 2] Vector collections for trucks and inbox
    private Vector<Truck> trucks;
    private Vector<String> inbox;
    private ColdTrackDao dao;
    private int badReadingCount;

    public ColdChainService(ColdTrackDao dao) {
        this.dao = dao;
        this.trucks = new Vector<Truck>();
        this.inbox = new Vector<String>();
        this.badReadingCount = 0;
    }

    public Vector<Truck> getTrucks() {
        return trucks;
    }

    public Vector<String> getInbox() {
        return inbox;
    }

    public synchronized void recordBadReading() {
        this.badReadingCount++;
    }

    // [Practical 4] Overloaded method 1: addTruck with default temperature bounds
    public synchronized Truck addTruck(String name, String cargo) {
        double min = 0.0;
        double max = 10.0;

        // [Practical 9] String method: equalsIgnoreCase
        if ("vaccine".equalsIgnoreCase(cargo)) {
            min = 2.0;
            max = 8.0;
        } else if ("frozen".equalsIgnoreCase(cargo)) {
            min = -20.0;
            max = -15.0;
        } else if ("food".equalsIgnoreCase(cargo)) {
            min = 0.0;
            max = 5.0;
        }

        return addTruck(name, cargo, min, max);
    }

    // [Practical 4] Overloaded method 2: addTruck with custom temperature bounds
    public synchronized Truck addTruck(String name, String cargo, double min, double max) {
        int nextId = 1;
        for (int i = 0; i < trucks.size(); i++) {
            if (trucks.elementAt(i).getId() >= nextId) {
                nextId = trucks.elementAt(i).getId() + 1;
            }
        }

        Truck t;
        if ("vaccine".equalsIgnoreCase(cargo)) {
            t = new VaccineTruck(nextId, name, min, max);
        } else if ("frozen".equalsIgnoreCase(cargo)) {
            t = new FrozenTruck(nextId, name, min, max);
        } else {
            t = new FoodTruck(nextId, name, min, max);
        }

        trucks.addElement(t);
        if (dao != null) {
            dao.saveTruck(t);
        }
        return t;
    }

    // [Practical 2] Insert truck at specific index in Vector
    public synchronized void insertTruck(int index, Truck t) {
        if (index >= 0 && index <= trucks.size()) {
            trucks.insertElementAt(t, index);
        } else {
            trucks.addElement(t);
        }
        if (dao != null) {
            dao.saveTruck(t);
        }
    }

    // Remove truck by ID
    public synchronized boolean removeTruck(int id) {
        for (int i = 0; i < trucks.size(); i++) {
            if (trucks.elementAt(i).getId() == id) {
                trucks.removeElementAt(i);
                if (dao != null) {
                    dao.deleteTruck(id);
                }
                return true;
            }
        }
        return false;
    }

    // Find truck or throw TruckNotFoundException
    public synchronized Truck findTruck(int id) throws TruckNotFoundException {
        for (int i = 0; i < trucks.size(); i++) {
            Truck t = trucks.elementAt(i);
            if (t.getId() == id) {
                return t;
            }
        }
        // [Practical 7] Throw custom exception when truck not found
        throw new TruckNotFoundException("Truck ID #" + id + " was not found in the monitor list.");
    }

    // [Practical 9] String methods: indexOf, substring, split, startsWith, trim, parseDouble
    public void processMessage(String msg) throws InvalidReadingException {
        if (msg == null) {
            throw new InvalidReadingException("Empty message payload received");
        }

        String trimmed = msg.trim();
        int pipeIdx = trimmed.indexOf('|');
        if (pipeIdx == -1) {
            throw new InvalidReadingException("Missing '|' delimiter in message: " + trimmed);
        }

        String idStr = trimmed.substring(0, pipeIdx).trim();
        String dataPart = trimmed.substring(pipeIdx + 1).trim();

        int truckId;
        try {
            truckId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new InvalidReadingException("Invalid numeric truck ID in payload: " + idStr);
        }

        String[] parts = dataPart.split(",");
        if (parts.length != 2) {
            throw new InvalidReadingException("Expected 2 parts (T and H) separated by comma: " + dataPart);
        }

        String tPart = parts[0].trim();
        String hPart = parts[1].trim();

        // [Practical 9] String method: startsWith and charAt
        if (!tPart.startsWith("T:") || !hPart.startsWith("H:")) {
            throw new InvalidReadingException("Malformed prefix in reading: " + dataPart);
        }

        double temp;
        double hum;
        try {
            temp = Double.parseDouble(tPart.substring(2).trim());
            hum = Double.parseDouble(hPart.substring(2).trim());
        } catch (NumberFormatException e) {
            throw new InvalidReadingException("Non-numeric sensor data: " + dataPart);
        }

        // Validate temperature and humidity bounds
        if (temp < -50.0 || temp > 60.0) {
            throw new InvalidReadingException("Temperature " + temp + " C out of realistic sensor range (-50 to 60)");
        }
        if (hum < 0.0 || hum > 100.0) {
            throw new InvalidReadingException("Humidity " + hum + " % out of range (0 to 100)");
        }

        // Find truck and apply reading
        Truck truck;
        try {
            truck = findTruck(truckId);
        } catch (TruckNotFoundException e) {
            throw new InvalidReadingException("Reading refers to non-existent truck: " + truckId);
        }

        String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            synchronized (truck) {
                truck.addReading(temp, hum);
            }
        } catch (TemperatureBreachException e) {
            // [Practical 7] Catch breach and raise system alert
            raiseAlert(truck, e.getMessage());
        }

        // Save reading to database
        if (dao != null) {
            Reading r = new Reading(0, truckId, temp, hum, truck.getStatus(), timeNow);
            dao.saveReading(r);
        }
    }

    // [Practical 4] Overriding raiseAlert from Alertable interface
    @Override
    public void raiseAlert(Truck t, String msg) {
        Alert.counter++;
        String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Alert a = new Alert(Alert.counter, t.getId(), msg, t.getCurrentTemp(), timeNow);

        System.out.println("[ALERT #" + a.getId() + "] " + msg + " at " + timeNow);
        if (dao != null) {
            dao.saveAlert(a);
        }
    }

    // [Practical 4] Overriding getSummary from Reportable interface
    @Override
    public synchronized String getSummary() {
        int total = trucks.size();
        // [Practical 2] Array: int[3] to hold status counts (0: OK, 1: WARNING, 2: DANGER)
        int[] counts = new int[3];

        for (int i = 0; i < total; i++) {
            Truck t = trucks.elementAt(i);
            String st = t.getStatus();
            if ("OK".equalsIgnoreCase(st)) {
                counts[0]++;
            } else if ("WARNING".equalsIgnoreCase(st)) {
                counts[1]++;
            } else {
                counts[2]++;
            }
        }

        // [Practical 1] Type casting: double to int for percentage
        int okPct = total > 0 ? (int) (((double) counts[0] / total) * 100.0) : 0;

        return "Total Trucks: " + total + " | OK: " + counts[0] + " (" + okPct + "%) | " +
               "WARNING: " + counts[1] + " | DANGER: " + counts[2] + " | Bad Readings Filtered: " + badReadingCount;
    }
}
