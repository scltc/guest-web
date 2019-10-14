package com.brickintellect.webserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

// Patterned after:
//   Practical Jackson ObjectMapper Configuration
//   https://www.stubbornjava.com/posts/practical-jackson-objectmapper-configuration
//   https://github.com/StubbornJava/StubbornJava/blob/master/stubbornjava-common/src/main/java/com/stubbornjava/common/Json.java

public class Json {

    public static class JsonException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private JsonException(Exception exception) {
            super(exception);
        }
    }

    public static class JsonType<T> {
        public TypeReference<T> typeReference = new TypeReference<T>() { };
    }

    public static class JsonSerializer {

        private final ObjectMapper mapper;
        private final ObjectWriter writer;

        public JsonSerializer(ObjectMapper mapper) {
            this.mapper = mapper;
            this.writer = mapper.writer();
        }

        public ObjectMapper mapper() {
            return mapper;
        }

        public String encodeString(Object value) {
            try {
                return writer.writeValueAsString(value);
            } catch (IOException exception) {
                throw new JsonException(exception);
            }
        }

        public <T> T decodeString(String value, JsonType<T> jsonType) {
            try {
                return mapper.readValue(value, jsonType.typeReference);
            } catch (IOException exception) {
                throw new JsonException(exception);
            }
        }
    }

    private static final JsonSerializer instance;

    public static ObjectMapper mapper() {
        return instance.mapper();
    }

    public static String encodeString(Object object) {
        return instance.encodeString(object);
    }

    public static <T> T decodeString(String value, JsonType<T> type) {
        return instance.decodeString(value, type);
    }

    static {
    
        ObjectMapper mapper = new ObjectMapper();

        // Don't throw an exception when json has extra fields you are
        // not serializing on. This is useful when you want to use a pojo
        // for deserialization and only care about a portion of the json
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Ignore null values when writing json.
        // mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        // mapper.setSerializationInclusion(Include.NON_NULL);

        // Write times as a String instead of a Long so its human readable.
        // mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // mapper.registerModule(new JavaTimeModule());

        // Custom serializer for coda hale metrics.
        // mapper.registerModule(new MetricsModule(TimeUnit.MINUTES,
        // TimeUnit.MILLISECONDS, false));

        instance = new JsonSerializer(mapper);
    }
}