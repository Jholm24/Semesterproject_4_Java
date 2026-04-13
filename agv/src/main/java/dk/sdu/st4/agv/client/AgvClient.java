package dk.sdu.st4.agv.client;

import dk.sdu.st4.core.exception.AgvException;
import dk.sdu.st4.core.model.AgvStatus;

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
        // TODO:
        //  1. Build HttpRequest.newBuilder(endpoint).GET().build()
        //  2. Send via httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        //  3. Check response status code (expect 200)
        //  4. Deserialise response body with JsonUtil.fromJson(body, AgvStatus.class)
        //     Note: JSON field "Program name" maps to AgvStatus.programName — configure Jackson
        //     with @JsonProperty or a custom deserialiser.
        //  5. Return the deserialised AgvStatus
        throw new UnsupportedOperationException("TODO: implement AgvClient.getStatus");
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
