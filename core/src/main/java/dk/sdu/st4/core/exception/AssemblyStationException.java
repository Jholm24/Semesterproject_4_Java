package dk.sdu.st4.core.exception;

/** Thrown when an Assembly Station MQTT operation fails. */
public class AssemblyStationException extends St4Exception {

    public AssemblyStationException(String message) {
        super(message);
    }

    public AssemblyStationException(String message, Throwable cause) {
        super(message, cause);
    }
}
