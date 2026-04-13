package dk.sdu.st4.core.exception;

/** Thrown when a Warehouse SOAP call fails or returns an error response. */
public class WarehouseException extends St4Exception {

    public WarehouseException(String message) {
        super(message);
    }

    public WarehouseException(String message, Throwable cause) {
        super(message, cause);
    }
}
