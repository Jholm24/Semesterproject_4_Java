package dk.sdu.st4.core.exception;

/**
 * Base checked exception for all ST4 integration failures.
 * Component-specific exceptions extend this class.
 */
public class St4Exception extends Exception {

    public St4Exception(String message) {
        super(message);
    }

    public St4Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
