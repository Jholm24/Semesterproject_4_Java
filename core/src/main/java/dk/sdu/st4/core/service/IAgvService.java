package dk.sdu.st4.core.service;

import dk.sdu.st4.core.enums.AgvProgram;
import dk.sdu.st4.core.exception.AgvException;
import dk.sdu.st4.core.model.AgvStatus;

/**
 * Contract for communicating with the AGV via its REST API.
 *
 * <p>Usage pattern (two-step "load then execute"):
 * <ol>
 *   <li>Call {@link #loadProgram(AgvProgram)} to stage the desired movement/operation.</li>
 *   <li>Call {@link #executeProgram()} to trigger execution.</li>
 *   <li>Poll {@link #getStatus()} until {@code State} returns IDLE to confirm completion.</li>
 * </ol>
 *
 * <p>REST endpoint: {@code PUT/GET http://localhost:8082/v1/status/}
 */
public interface IAgvService {

    /**
     * Loads (stages) a program on the AGV without starting it.
     * Sends {@code {"Program name": "<program>", "State": 1}}.
     *
     * @param program the program to load
     * @throws AgvException if the HTTP call fails or the AGV returns an error state
     */
    void loadProgram(AgvProgram program) throws AgvException;

    /**
     * Executes the currently loaded program.
     * Sends {@code {"State": 2}}.
     *
     * @throws AgvException if the HTTP call fails or no program has been loaded
     */
    void executeProgram() throws AgvException;

    /**
     * Retrieves the current AGV status (battery, program, state, timestamp).
     *
     * @return latest {@link AgvStatus} snapshot
     * @throws AgvException if the HTTP call fails
     */
    AgvStatus getStatus() throws AgvException;
}
