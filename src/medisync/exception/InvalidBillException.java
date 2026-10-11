package medisync.exception;

// [Practical 5] Packages: medisync.exception
// [Practical 7] Custom checked exception
public class InvalidBillException extends Exception {
    public InvalidBillException(String message) {
        super(message);
    }
}
