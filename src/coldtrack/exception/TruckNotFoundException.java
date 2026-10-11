package coldtrack.exception;

// [Practical 5] Packages: coldtrack.exception
// [Practical 7] Custom user-defined Exception when truck ID is not found
public class TruckNotFoundException extends Exception {
    public TruckNotFoundException(String msg) {
        super(msg);
    }
}
