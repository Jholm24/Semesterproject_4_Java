package dk.sdu.st4.agv.client;

import dk.sdu.st4.common.util.JsonUtil;
import dk.sdu.st4.common.data.AgvStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Low-level HTTP client for the AGV REST API.
 *
 * <p>Both operations (load and execute) use HTTP PUT to the same endpoint.
 * Status retrieval uses HTTP GET.
 *
 * <p>Endpoint: {@code http://localhost:8082/v1/status/}
 */
public class AgvClient {

    private final HttpClient httpClient;
    private final URI endpoint;

    public AgvClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.endpoint = URI.create(baseUrl);
    }

    /**
     * Sends an HTTP GET to the AGV endpoint and returns the parsed {@link AgvStatus}.
     *
     * @return current AGV status
     * @throws Exception if the request fails or the response cannot be parsed
     */
    public AgvStatus getStatus() throws Exception {
        // 1. Build the GET request
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .GET()
                .build();

        // 2. Send the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 3. Check response status code
        if (response.statusCode() != 200) {
            throw new Exception("Unexpected status code: " + response.statusCode());
        }

        // 4. Deserialize and return
        return JsonUtil.fromJson(response.body(), AgvStatus.class);
    }

    /**
     * Sends an HTTP PUT to the AGV endpoint with the given JSON body.
     * Used for both load-program and execute-program requests.
     *
     * @param jsonBody request payload, e.g. {@code {"Program name":"...", "State":1}}
     * @throws Exception if the request fails or the response cannot be parsed
     */
    public void sendPut(String jsonBody) throws Exception {
        // 1. Build PUT request with jsonBody as body publisher
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();

        // 2. Send the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 3. Check response status code
        if (response.statusCode() != 200) {
            throw new Exception("Unexpected status code: " + response.statusCode());
        }

        // 4. Deserialize and return AgvStatus
        JsonUtil.fromJson(response.body(), AgvStatus.class);
    }
}
