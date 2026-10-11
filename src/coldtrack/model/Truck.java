package coldtrack.model;

import coldtrack.exception.TemperatureBreachException;
import coldtrack.service.Monitorable;

// [Practical 4] Abstract class implementing Monitorable interface
// [Practical 5] Packages: coldtrack.model
public abstract class Truck implements Monitorable {

    // [Practical 6] final constants
    public static final int MAX_READINGS = 10;
    public static final int ALERT_AFTER = 3;

    // Encapsulation: private fields
    private int id;
    private String name;
    private String cargo;
    private double minTemp;
    private double maxTemp;
    private double currentTemp;
    private double currentHumidity;
    private String status;
    private int dangerCount;
    private boolean failing;
    // [Practical 2] Array for last 10 readings
    private double[] last10;
    private boolean alerted;

    // [Practical 3] Default constructor
    // [Practical 6] this() constructor chaining
    public Truck() {
        this(0, "Unnamed", "general", 0.0, 10.0);
    }

    // [Practical 3] Full parameterized constructor
    public Truck(int id, String name, String cargo, double minTemp, double maxTemp) {
        this.id = id;
        setName(name);
        setCargo(cargo);
        setLimits(minTemp, maxTemp);
        this.currentTemp = (minTemp + maxTemp) / 2.0;
        this.currentHumidity = 55.0;
        this.status = "OK";
        this.dangerCount = 0;
        this.failing = false;
        this.alerted = false;
        this.last10 = new double[MAX_READINGS];
        // Initialize last10 array with midpoint temperature
        for (int i = 0; i < MAX_READINGS; i++) {
            this.last10[i] = this.currentTemp;
        }
    }

    // Setter validations
    public void setName(String name) {
        // [Practical 9] String method: trim and length
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Truck name cannot be empty");
        }
        this.name = name.trim();
    }

    public void setLimits(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("minTemp must be strictly less than maxTemp");
        }
        this.minTemp = min;
        this.maxTemp = max;
    }

    // [Practical 4] Overloaded method 1: addReading with temperature only
    public void addReading(double temp) throws TemperatureBreachException {
        addReading(temp, this.currentHumidity);
    }

    // [Practical 4] Overloaded method 2: addReading with temperature and humidity
    public void addReading(double temp, double humidity) throws TemperatureBreachException {
        // [Practical 2] Shift old values in last10 array
        for (int i = last10.length - 1; i > 0; i--) {
            last10[i] = last10[i - 1];
        }
        last10[0] = temp;

        this.currentTemp = temp;
        this.currentHumidity = humidity;

        // [Practical 4] Polymorphic call to checkLimit overridden in subclasses
        this.status = checkLimit(temp);

        // [Practical 1] Control flow: if-else
        // [Practical 9] String method: equalsIgnoreCase
        if ("DANGER".equalsIgnoreCase(this.status)) {
            this.dangerCount++;
            if (this.dangerCount >= ALERT_AFTER && !this.alerted) {
                this.alerted = true;
                // [Practical 7] Throw custom TemperatureBreachException
                throw new TemperatureBreachException(name + " in DANGER for 3 readings, temperature " + temp);
            }
        } else {
            this.dangerCount = 0;
            this.alerted = false;
        }
    }

    // [Practical 6] final method: cannot be overridden by subclasses
    public final String getColor() {
        if ("OK".equalsIgnoreCase(status)) {
            return "green";
        } else if ("WARNING".equalsIgnoreCase(status)) {
            return "orange";
        } else {
            return "red";
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo != null ? cargo.trim().toLowerCase() : "general";
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = currentTemp;
    }

    public double getCurrentHumidity() {
        return currentHumidity;
    }

    public void setCurrentHumidity(double currentHumidity) {
        this.currentHumidity = currentHumidity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDangerCount() {
        return dangerCount;
    }

    public boolean isFailing() {
        return failing;
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    public double[] getLast10() {
        return last10;
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void setAlerted(boolean alerted) {
        this.alerted = alerted;
    }
}
