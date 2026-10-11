package medisync.exception;

// [Practical 5] Packages: medisync.exception
// [Practical 7] Custom checked exception
public class InvalidPatientException extends Exception {
    public InvalidPatientException(String message) {
        super(message);
    }
}
