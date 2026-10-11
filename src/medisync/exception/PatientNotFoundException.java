package medisync.exception;

// [Practical 5] Packages: medisync.exception
// [Practical 7] Custom checked exception
public class PatientNotFoundException extends Exception {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
