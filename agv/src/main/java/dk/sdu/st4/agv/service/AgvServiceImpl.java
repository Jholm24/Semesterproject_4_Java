package dk.sdu.st4.agv.service;

import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.core.enums.AgvProgram;
import dk.sdu.st4.core.model.AgvStatus;
import dk.sdu.st4.common.Interfaces.IAgv;

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
        // TODO:
        //  Build JSON body:  {"Program name": program.getApiName(), "State": AppConfig.AGV_LOAD_STATE}
        //  Call client.sendPut(body) and verify the returned state indicates load accepted.
        throw new UnsupportedOperationException("TODO: implement AgvServiceImpl.loadProgram");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends: {@code {"State": 2}}
     */
    @Override
    public void executeProgram() throws Exception {
        // TODO:
        //  Build JSON body:  {"State": AppConfig.AGV_EXECUTE_STATE}
        //  Call client.sendPut(body).
        throw new UnsupportedOperationException("TODO: implement AgvServiceImpl.executeProgram");
    }

    /** {@inheritDoc} */
    @Override
    public AgvStatus getStatus() throws Exception {
        // TODO: Delegate to client.getStatus()
        throw new UnsupportedOperationException("TODO: implement AgvServiceImpl.getStatus");
    }
}
