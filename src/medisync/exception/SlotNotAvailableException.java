package medisync.exception;

// [Practical 5] Packages: medisync.exception
// [Practical 7] Custom checked exception
public class SlotNotAvailableException extends Exception {
    public SlotNotAvailableException(String message) {
        super(message);
    }
}
