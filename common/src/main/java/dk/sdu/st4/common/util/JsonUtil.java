package dk.sdu.st4.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin wrapper around Jackson's {@link ObjectMapper} for JSON serialisation
 * and deserialisation used across the component modules.
 *
 * <p>The single shared {@code ObjectMapper} instance is thread-safe once configured.
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
        // utility class — not instantiable
    }

    /**
     * Serialises {@code object} to a JSON string.
     *
     * @param object the object to serialise
     * @return JSON representation
     * @throws RuntimeException wrapping {@link com.fasterxml.jackson.core.JsonProcessingException}
     *                          if serialisation fails
     */
    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialises a JSON string to an instance of {@code type}.
     *
     * @param json  the JSON string to parse
     * @param type  the target class
     * @param <T>   the target type
     * @return deserialised instance
     * @throws RuntimeException wrapping {@link com.fasterxml.jackson.core.JsonProcessingException}
     *                          if deserialisation fails
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
