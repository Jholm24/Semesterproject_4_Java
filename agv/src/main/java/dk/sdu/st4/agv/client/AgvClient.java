package dk.sdu.st4.agv.client;

import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AgvClient implements IAgv {

    private final HttpClient httpClient;
    private final URI        endpoint;

    public AgvClient() {
        this(AppConfig.AGV_BASE_URL);
    }

    public AgvClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.endpoint   = URI.create(baseUrl);
    }

    @Override
    public void loadProgram(AgvProgram program) throws Exception {
        var body = String.format("{\"Program name\": \"%s\", \"State\": %d}",
                program.getProgram(), AppConfig.AGV_LOAD_STATE);
        sendPut(body);
    }

    @Override
    public void executeProgram() throws Exception {
        var body = String.format("{\"State\": %d}", AppConfig.AGV_EXECUTE_STATE);
        sendPut(body);
    }

    @Override
    public AgvStatus getStatus() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(endpoint).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("GET " + endpoint + " → " + response.statusCode() + ": " + response.body());
        }
        return JsonUtil.fromJson(response.body(), AgvStatus.class);
    }

    private void sendPut(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("PUT " + endpoint + " body=" + jsonBody + " → " + response.statusCode() + ": " + response.body());
        }
    }
}
