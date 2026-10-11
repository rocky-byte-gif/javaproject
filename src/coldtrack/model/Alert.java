package coldtrack.model;

// [Practical 3] Model class for alert entity
// [Practical 5] Packages: coldtrack.model
public class Alert {
    // [Practical 6] static counter for alert IDs initialized from DB
    public static int counter = 0;

    private int id;
    private int truckId;
    private String message;
    private double temperature;
    private String timeText;

    // [Practical 3] Default constructor
    public Alert() {
        this(0, 0, "", 0.0, "");
    }

    // [Practical 3] Parameterized constructor
    public Alert(int id, int truckId, String message, double temperature, String timeText) {
        this.id = id;
        this.truckId = truckId;
        this.message = message;
        this.temperature = temperature;
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

    public String getMessage() {
        return message;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getTimeText() {
        return timeText;
    }
}
