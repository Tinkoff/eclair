package ru.tinkoff.integration.eclair.deprecated.splunk;

import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Component
class StringConverter {

    List<String> convert(Object[] arguments) {
        return stream(arguments).map(this::convert).collect(toList());
    }

    String convert(Object argument) {
        if (argument instanceof String) {
            return "'" + argument + "'";
        }
        if (argument instanceof Number) {
            return "<" + argument + ">";
        }
        if (argument instanceof byte[]) {
            return ((byte[]) argument).length == 0 ? "[]" : "[...]";
        }
        return isNull(argument) ? "null" : argument.toString();
    }
}
