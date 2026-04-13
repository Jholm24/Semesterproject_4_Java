package dk.sdu.st4.agv.client;

import dk.sdu.st4.common.util.JsonUtil;
import dk.sdu.st4.core.exception.AgvException;
import dk.sdu.st4.core.model.AgvStatus;

import java.io.IOException;
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
     * @throws AgvException if the request fails or the response cannot be parsed
     */
    public AgvStatus getStatus() throws AgvException {
        try {
            // 1. Build the GET request
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .GET()
                    .build();

            // 2. Send the request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Check response status code
            if (response.statusCode() != 200) {
                throw new AgvException("Unexpected status code: " + response.statusCode());
            }

            // 4. Deserialize response body
            AgvStatus status = JsonUtil.fromJson(response.body(), AgvStatus.class);

            // 5. Return the deserialized AgvStatus
            return status;
        }
        catch (IOException  | InterruptedException e) {
            throw new AgvException("Failed to get AGV status: " + e.getMessage(), e);

        }
    }

    /**
     * Sends an HTTP PUT to the AGV endpoint with the given JSON body.
     * Used for both load-program and execute-program requests.
     *
     * @param jsonBody request payload, e.g. {@code {"Program name":"...", "State":1}}
     * @return AGV status from the PUT response body
     * @throws AgvException if the request fails or the response cannot be parsed
     */
    public AgvStatus sendPut(String jsonBody) throws AgvException {
        // TODO:
        //  1. Build HttpRequest with PUT method and jsonBody as body publisher
        //     (Content-Type: application/json)
        //  2. Send via httpClient.send(...)
        //  3. Check response status code (expect 200)
        //  4. Deserialise and return AgvStatus from response body
        throw new UnsupportedOperationException("TODO: implement AgvClient.sendPut");
    }
}
