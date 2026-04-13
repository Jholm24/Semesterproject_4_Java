package dk.sdu.st4.core.enums;

/**
 * Operational states broadcast by the Assembly Station over MQTT.
 *
 * Appears in the {@code emulator/status} topic payload under "State" (integer value).
 */
public enum AssemblyState {

    IDLE(0),
    EXECUTING(1),
    ERROR(2);

    private final int code;

    AssemblyState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AssemblyState fromCode(int code) {
        for (AssemblyState s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown Assembly state code: " + code);
    }
}
