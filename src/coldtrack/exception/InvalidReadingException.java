package coldtrack.exception;

// [Practical 5] Packages: coldtrack.exception
// [Practical 7] Custom user-defined Exception for malformed readings
public class InvalidReadingException extends Exception {
    public InvalidReadingException(String msg) {
        super(msg);
    }
}
