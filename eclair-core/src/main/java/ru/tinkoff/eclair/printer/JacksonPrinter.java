package ru.tinkoff.eclair.printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Viacheslav Klapatniuk
 */
public class JacksonPrinter extends Printer {

    private final ObjectMapper objectMapper;

    public JacksonPrinter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected String serialize(Object input) throws IllegalArgumentException {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
