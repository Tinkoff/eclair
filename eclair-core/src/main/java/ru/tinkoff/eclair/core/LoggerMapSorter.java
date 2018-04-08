package ru.tinkoff.eclair.core;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

/**
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
 */
public class LoggerMapSorter {

    public Map<String, EclairLogger> sort(Map<String, EclairLogger> input) {
        return input.entrySet().stream()
                .sorted(comparing(Map.Entry::getValue, AnnotationAwareOrderComparator.INSTANCE))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (logger, logger2) -> logger,
                        LinkedHashMap::new
                ));
    }
}
