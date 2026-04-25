package dk.sdu.st4.agv.service;

import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.db.DBMachineService;
import dk.sdu.st4.common.services.IAgv;

public class AgvServiceImpl extends DBMachineService implements IAgv {

    private volatile AgvClient activeClient;

    /** Default constructor — no active machine until connectMachine is called. */
    public AgvServiceImpl() {
        super("AGV");
    }

    /** Constructor for a fixed URL (bypasses DB — useful for tests). */
    public AgvServiceImpl(String baseUrl) {
        super("AGV");
        this.activeClient = new AgvClient(baseUrl);
    }

    // ── DBMachineService hooks ────────────────────────────────────────────────

    @Override
    protected void onConnect(String serialNo, String baseUrl) throws Exception {
        AgvClient client = new AgvClient(baseUrl);
        client.getStatus();  // verify reachability
        this.activeClient = client;
    }

    @Override
    protected void onDisconnect(String serialNo) {
        this.activeClient = null;
    }

    // ── IAgv ─────────────────────────────────────────────────────────────────

    @Override
    public void loadProgram(AgvProgram program) throws Exception {
        var body = String.format("{\"Program name\": \"%s\", \"State\": %d}",
            program.getProgram(), AppConfig.AGV_LOAD_STATE);
        requireClient().sendPut(body);
    }

    @Override
    public void executeProgram() throws Exception {
        var body = String.format("{\"State\": %d}", AppConfig.AGV_EXECUTE_STATE);
        requireClient().sendPut(body);
    }

    @Override
    public AgvStatus getStatus() throws Exception {
        return requireClient().getStatus();
    }

    private AgvClient requireClient() {
        AgvClient c = activeClient;
        if (c == null) throw new IllegalStateException("No AGV connected — call connectMachine first");
        return c;
    }
}
