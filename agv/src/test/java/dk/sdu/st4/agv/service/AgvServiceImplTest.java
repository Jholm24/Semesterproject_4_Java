package dk.sdu.st4.agv.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.enums.AgvState;
import dk.sdu.st4.common.data.AgvStatus;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for {@link AgvServiceImpl} using WireMock to stub
 * the AGV REST API.  Each test verifies both the HTTP request shape (method,
 * headers, body) and the parsing of the HTTP response.
 */
class AgvServiceImplTest {

    private static final String AGV_PATH = "/v1/status/";

    /** A sample valid AGV JSON response with State=1 (Idle). */
    private static final String IDLE_RESPONSE = """
            {"battery":80,"program name":"MoveToStorageOperation","state":1,"timestamp":"12:00:00"}
            """.strip();

    /** A sample valid AGV JSON response with State=2 (Executing). */
    private static final String EXECUTING_RESPONSE = """
            {"battery":75,"program name":"MoveToAssemblyOperation","state":2,"timestamp":"12:01:00"}
            """.strip();

    private static WireMockServer wireMock;
    private AgvServiceImpl service;

    @BeforeAll
    static void startServer() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopServer() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        service = new AgvServiceImpl("http://localhost:" + wireMock.port() + AGV_PATH);
    }

    // -------------------------------------------------------------------------
    // getStatus
    // -------------------------------------------------------------------------

    @Test
    void getStatus_returnsFullyPopulatedStatus_whenServerResponds200() throws Exception {
        wireMock.stubFor(get(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IDLE_RESPONSE)));

        AgvStatus status = service.getStatus();

        assertEquals(80, status.getBattery());
        assertEquals("MoveToStorageOperation", status.getProgramName());
        assertEquals(AgvState.Idle, status.getState());
        assertEquals("12:00:00", status.getTimestamp());
    }

    @Test
    void getStatus_sendsGetRequest_toCorrectEndpoint() throws Exception {
        wireMock.stubFor(get(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IDLE_RESPONSE)));

        service.getStatus();

        wireMock.verify(getRequestedFor(urlEqualTo(AGV_PATH)));
    }

    @Test
    void getStatus_throwsException_whenServerReturns500() {
        wireMock.stubFor(get(urlEqualTo(AGV_PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(Exception.class, () -> service.getStatus());
    }

    @Test
    void getStatus_mapsExecutingState_correctly() throws Exception {
        wireMock.stubFor(get(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(EXECUTING_RESPONSE)));

        AgvStatus status = service.getStatus();

        assertEquals(AgvState.Executing, status.getState());
    }

    // -------------------------------------------------------------------------
    // loadProgram
    // -------------------------------------------------------------------------

    @Test
    void loadProgram_sendsPutWithJsonContentType() throws Exception {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IDLE_RESPONSE)));

        service.loadProgram(AgvProgram.MoveToStorageOperation);

        wireMock.verify(putRequestedFor(urlEqualTo(AGV_PATH))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void loadProgram_sendsState1InBody() throws Exception {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IDLE_RESPONSE)));

        service.loadProgram(AgvProgram.MoveToStorageOperation);

        wireMock.verify(putRequestedFor(urlEqualTo(AGV_PATH))
                .withRequestBody(containing("\"State\": 1")));
    }

    @Test
    void loadProgram_sendsProgramNameInBody() throws Exception {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IDLE_RESPONSE)));

        service.loadProgram(AgvProgram.MoveToStorageOperation);

        wireMock.verify(putRequestedFor(urlEqualTo(AGV_PATH))
                .withRequestBody(containing("\"Program name\""))
                .withRequestBody(containing(AgvProgram.MoveToStorageOperation.getProgram())));
    }

    @Test
    void loadProgram_throwsException_whenServerReturns500() {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(Exception.class, () -> service.loadProgram(AgvProgram.MoveToAssemblyOperation));
    }

    // -------------------------------------------------------------------------
    // executeProgram
    // -------------------------------------------------------------------------

    @Test
    void executeProgram_sendsPutWithJsonContentType() throws Exception {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(EXECUTING_RESPONSE)));

        service.executeProgram();

        wireMock.verify(putRequestedFor(urlEqualTo(AGV_PATH))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void executeProgram_sendsState2InBody() throws Exception {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(EXECUTING_RESPONSE)));

        service.executeProgram();

        wireMock.verify(putRequestedFor(urlEqualTo(AGV_PATH))
                .withRequestBody(containing("\"State\": 2")));
    }

    @Test
    void executeProgram_throwsException_whenServerReturns500() {
        wireMock.stubFor(put(urlEqualTo(AGV_PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(Exception.class, () -> service.executeProgram());
    }
}
