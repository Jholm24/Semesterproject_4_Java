package dk.sdu.st4.common.data.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Operational states reported by the AGV REST API.
 *
 * JSON field: "State" (integer value)
 */
public enum AgvState {

    Idle(1),
    Executing(2),
    Charging(3);

    private final int state;

    AgvState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    /** Resolves a raw integer code to the corresponding enum constant. */
    @JsonCreator
    public static AgvState fromState(int state) {
        for (AgvState s : values()) {
            if (s.state == state) return s;
        }
        throw new IllegalArgumentException("Unknown AGV state code: " + state);
    }
}
