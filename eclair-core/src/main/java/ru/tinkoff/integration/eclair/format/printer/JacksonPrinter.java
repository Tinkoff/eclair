package ru.tinkoff.integration.eclair.format.printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json -> throws
 */
public class JacksonPrinter implements Printer {

    private final ObjectMapper objectMapper;

    public JacksonPrinter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String print(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
