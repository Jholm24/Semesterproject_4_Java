package dk.sdu.st4.core.enums;

/**
 * Operational states reported by the AGV REST API.
 *
 * JSON field: "State" (integer value)
 */
public enum AgvState {

    IDLE(1),
    EXECUTING(2),
    CHARGING(3);

    private final int code;

    AgvState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /** Resolves a raw integer code to the corresponding enum constant. */
    public static AgvState fromCode(int code) {
        for (AgvState s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown AGV state code: " + code);
    }
}
