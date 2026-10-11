package grievance.exception;

// [Practical 5] Packages: grievance.exception
// [Practical 7] Custom user-defined Exception
public class ComplaintNotFoundException extends Exception {

    public ComplaintNotFoundException(String message) {
        // [Practical 6] Passing message to superclass constructor
        super(message);
    }
}
