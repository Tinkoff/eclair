package ru.tinkoff.eclair.format.printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json -> throws
 *
 * @author Viacheslav Klapatniuk
 */
public class JacksonPrinter extends Printer {

    private final ObjectMapper objectMapper;

    public JacksonPrinter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
