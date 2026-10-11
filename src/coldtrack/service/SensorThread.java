package coldtrack.service;

import java.util.Vector;
import coldtrack.model.Truck;

// [Practical 5] Packages: coldtrack.service
// [Practical 8] Multithreading: extends Thread
public class SensorThread extends Thread {

    private Truck truck;
    private Vector<String> inbox;
    private boolean active;
    private double currentTemp;
    private double currentHumidity;
    private int stepCount;

    public SensorThread(Truck truck, Vector<String> inbox) {
        this.truck = truck;
        this.inbox = inbox;
        this.active = true;
        this.currentTemp = (truck.getMinTemp() + truck.getMaxTemp()) / 2.0;
        this.currentHumidity = 55.0;
        this.stepCount = 0;
    }

    public void stopSensor() {
        this.active = false;
    }

    // [Practical 8] Thread execution run method
    @Override
    public void run() {
        while (active) {
            try {
                // Read sensor reading every 2 seconds
                Thread.sleep(2000);

                stepCount++;
                String dataPart;

                // Simulate bad sensor payload every ~25 readings to showcase exception handling
                if (stepCount % 25 == 0) {
                    dataPart = "T:abc,H:60";
                } else {
                    if (truck.isFailing()) {
                        // Truck refrigeration failing: temperature rises by ~0.8 each step
                        currentTemp += 0.8;
                        currentHumidity += (Math.random() - 0.5) * 2.0;
                    } else {
                        // Normal condition: moves slightly around current temperature
                        double mid = (truck.getMinTemp() + truck.getMaxTemp()) / 2.0;
                        double drift = (Math.random() - 0.5) * 0.4;
                        // Tendency to stay near safe midpoint
                        if (currentTemp > mid + 1.0) {
                            currentTemp -= 0.3;
                        } else if (currentTemp < mid - 1.0) {
                            currentTemp += 0.3;
                        } else {
                            currentTemp += drift;
                        }
                        currentHumidity += (Math.random() - 0.5) * 1.5;
                    }

                    // Format with 1 decimal place
                    double roundedTemp = Math.round(currentTemp * 10.0) / 10.0;
                    double roundedHum = Math.round(currentHumidity * 10.0) / 10.0;
                    dataPart = "T:" + roundedTemp + ",H:" + roundedHum;
                }

                String payload = truck.getId() + "|" + dataPart;

                // [Practical 8] Synchronized block on shared inbox Vector
                synchronized (inbox) {
                    inbox.addElement(payload);
                }

            } catch (InterruptedException e) {
                System.out.println("[Sensor #" + truck.getId() + "] Interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[Sensor #" + truck.getId() + "] Error: " + e.getMessage());
            }
        }
    }
}
