package dk.sdu.st4.core.exception;

/** Thrown when an AGV REST API call fails or returns an unexpected state. */
public class AgvException extends St4Exception {

    public AgvException(String message) {
        super(message);
    }

    public AgvException(String message, Throwable cause) {
        super(message, cause);
    }
}
