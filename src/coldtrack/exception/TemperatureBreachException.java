package coldtrack.exception;

// [Practical 5] Packages: coldtrack.exception
// [Practical 7] Custom user-defined Exception for temperature breach alert
public class TemperatureBreachException extends Exception {
    public TemperatureBreachException(String msg) {
        super(msg);
    }
}
