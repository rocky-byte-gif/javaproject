package coldtrack.model;

// [Practical 3] Model class for sensor reading entity
// [Practical 5] Packages: coldtrack.model
public class Reading {
    private int id;
    private int truckId;
    private double temperature;
    private double humidity;
    private String status;
    private String timeText;

    // [Practical 3] Default constructor
    public Reading() {
        this(0, 0, 0.0, 0.0, "OK", "");
    }

    // [Practical 3] Parameterized constructor
    public Reading(int id, int truckId, double temperature, double humidity, String status, String timeText) {
        this.id = id;
        this.truckId = truckId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.status = status;
        this.timeText = timeText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTruckId() {
        return truckId;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeText() {
        return timeText;
    }
}
