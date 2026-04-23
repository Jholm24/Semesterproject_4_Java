package dk.sdu.st4.agv.service;

import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.services.IAgv;

/**
 * REST-backed implementation of {@link IAgv}.
 *
 * <p>Operation workflow (two-step "load then execute"):
 * <ol>
 *   <li>{@link #loadProgram(AgvProgram)} — PUT {@code {"Program name": "...", "State": 1}}</li>
 *   <li>{@link #executeProgram()}        — PUT {@code {"State": 2}}</li>
 *   <li>Poll {@link #getStatus()} until state is IDLE (1) to confirm completion.</li>
 * </ol>
 */
public class AgvServiceImpl implements IAgv {

    private final AgvClient client;

    /** Creates a service instance targeting the default AGV URL from {@link AppConfig}. */
    public AgvServiceImpl() {
        this(AppConfig.AGV_BASE_URL);
    }

    /** Creates a service instance targeting a custom AGV base URL (useful for testing). */
    public AgvServiceImpl(String baseUrl) {
        this.client = new AgvClient(baseUrl);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends: {@code {"Program name": "<program.apiName>", "State": 1}}
     */
    @Override
    public void loadProgram(AgvProgram program) throws Exception {
         var body = String.format("{\"Program name\": \"%s\", \"State\": %d}", program.getProgram(), AppConfig.AGV_LOAD_STATE);
        client.sendPut(body);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends: {@code {"State": 2}}
     */
    @Override
    public void executeProgram() throws Exception {
        var body = String.format("{\"State\": %d}", AppConfig.AGV_EXECUTE_STATE);
        client.sendPut(body);
    }

    /** {@inheritDoc} */
    @Override
    public AgvStatus getStatus() throws Exception {
        return client.getStatus();
    }
}
